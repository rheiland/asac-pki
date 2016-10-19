package base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import base.SimLogger.Log;
import measures.TimeMeasure;

/**
 * This class represents a given implementation's simulation.
 * @author yechen
 *
 */
public class System implements Measurable {
	private Map<String, ActorMachine> actors = new HashMap<String, ActorMachine>();
	//LinkedHashSet preserves orders of insertion.
	private LinkedHashSet<Workflow> workflows = new LinkedHashSet<Workflow>();
	private LinkedHashSet<Workflow> completedWorkflows = new LinkedHashSet<Workflow>();
	public Implementation impl;
	public ArrayList<Measure> measures = new ArrayList<Measure>();
	private Measure timeMeasure = new TimeMeasure();
	private Params param;
	private Action a; //current action
	private ActorMachine curActor;
	LinkedHashSet<Property> measurables = new LinkedHashSet<Property>();

	public boolean TIME_STOPPED = false;

	public String getImplementationName() {
		return impl.getClass().getCanonicalName();
	}

	/**
	 * This query returns an Action object which represents the current action in advance().
	 */
	public static final String Q_CURACTION = "CurrentAction";

	/**
	 * Which workflow does a particular action reside in.
	 * If no existing workflow is satisfiable given the action,
	 * this is where we create a new workflow.
	 * @param a action to be tested
	 * @return the workflow a resides in.
	 */
	private Workflow context(Action a) {
		String workflowClassName = param.workflowName;
		//Independent actions may be part of some other workflow, fill them in first.
		for(Workflow w : workflows) {
			if(w.isSatisfiable(a)) {
				return w;
			}
		}
		//The action do not satisfy any existing workflow. We check whether the action is independent.
		//If it is, we start a new workflow with head = this action.
		try {
			Class workflowClass = Class.forName(workflowClassName);
			Workflow w = (Workflow) workflowClass.newInstance();
			w.sys = this;

			if(w.isIndependentAction(a) && w.isSatisfiable(a)) {
				workflows.add(w);
				SimLogger.log(Level.FINEST, "New workflow created with action " + a);
				return w;
			}
		} catch(ClassNotFoundException e) {
			SimLogger.log(Level.SEVERE, "Class not found for workflow class: " + workflowClassName);
			e.printStackTrace();
		} catch(InstantiationException e) {
			SimLogger.log(Level.SEVERE, "Default Constructor not found for workflow class: " + workflowClassName);
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			SimLogger.log(Level.SEVERE, "Default Constructor not found for workflow class: " + workflowClassName);
			e.printStackTrace();
		}

		SimLogger.log(Level.FINE, "No workflow can be found to satisify action " + a +
		              ". Action is execution is cancelled.");
		return null;
	}

	/**
	 * This is used as a tester stub for deterministic tester.
	 * Also used as part of advance, it will take measurement and execute a given command in both
	 * workload and AC.
	 * @param a
	 */
	public void deterministicExecute(Action a) {
		SimLogger.log(Level.FINE, "Deterministic Execute Target: " + a);
		this.a = a;
		findMeasurables();
		for(Measure m : measures) {
			if(m.overridesPreExecMeasurement()) {
				if(m.isMeasurementvalid(this)) {
					m.preExecMeasurement(this);
				} else {
					SimLogger.log(Level.INFO, "Cannot take measurement #1 of "
								  + m.getPrintFriendlyName() + " for implementation "
								  + impl.getClass().getName());
				}
			}
		}

		// Turn on I/O accounting
		Implementation.accountIO = true;
		impl.action(a);
		Implementation.accountIO = false;
		// Turn off I/O accounting

		findMeasurables();
		for(Measure m : measures) {
			if(m.overridesPostExecMeasurement()) {
				if(m.isMeasurementvalid(this)) {
					m.postExecMeasurement(this);
				} else {
					// e.g. Cannot take measurement #2 of Bias toward additive commands for implementation web.ImplementPkiWInOcsp
					SimLogger.log(Level.INFO, "Cannot take measurement #2 of "
								  + m.getPrintFriendlyName() + " for implementation "
								  + impl.getClass().getName());
				}
			}
		}
	}


	/**
	 * Figure out the next action and workflow it resides in, execute the action while
	 * taking measurements.  rwh: called by Simulation
	 * @param time
	 */
	public void advance(double time) {
		Set<String> currentActors = new LinkedHashSet<String>(actors.keySet());
		for(String actor: currentActors) {
			Log.v("Want actor " + actor);
			ActorMachine m = actors.get(actor);
			Log.v("Found actor " + actor);
			if(m != null && m.time < time) {
				a = m.nextAction(param);
				curActor = m;
				if(a != null) {
					Log.v("Returned action is: " + a);
					Workflow w = context(a);
					if(w != null) {
						Log.v("Workflow returned: " + w);
						a = impl.addParameters(a, w);
						if(a != null) {
							Log.v("Parameter'd action is: " + a);

							//m.transition(a);
							timeMeasure.preExecMeasurement(this);
							m.transition(a);
							m.occupy((Integer) timeMeasure.getCurCost());
							w.execute(a);
							if(w.isComplete()) {
								workflows.remove(w);
								completedWorkflows.add(w);
							}
							deterministicExecute(a);
						} else {
							Log.d("Null action returned from addParameters.");
						}
					} else {
						Log.d("Null workflow returned from context");
					}
				} else {
					Log.d("No action returned by actor");
				}
			} else {
				Log.d("Actor " + actor + " too busy or deleted");
			}
		}

		if(SimLogger.LOGLEVEL == Level.FINEST) {
			String str = "";
			str += "Currently incomplete workflows: \n";
			for(Workflow w : workflows) {
				str += "" + w + "\n";
			}
			str += "Currently completed workflows: \n";
			for(Workflow w : completedWorkflows) {
				str += "" + w + "\n";
			}
			SimLogger.log(Level.FINEST, str);
		}
	}

	/**
	 * Constructor of System will build up actors, impl, measures.
	 * @param implClassName The class name of implementation we are building right now.
	 * @param param All other parameters.
	 */
	public System(String implClassName, Params param) {
		SimLogger.log(Level.INFO, "New System Created.");
		this.param = param;
		this.param.system = this;
		try {
			Class implClass = Class.forName(implClassName);
			impl = (Implementation) implClass.newInstance();
			// Set the implementation's system so init() can set it in workload!!!
			impl.sys = this;
			impl.init(param.starter.build());
			SimLogger.log(Level.FINE, "Implementation " + implClassName + " was created successfully.");

			try {
				for(String s : param.measures) {
					SimLogger.log(Level.FINE, "Adding measure " + s + " to system.");
					Class measureClass = Class.forName(s);
					Measure measure = (Measure) measureClass.newInstance();
					measures.add(measure);
				}
				Collections.sort(measures);
			} catch(ClassNotFoundException e) {
				SimLogger.log(Level.SEVERE, "Cannot find measure class with name = " + implClassName);
				throw new RuntimeException(e);
			} catch(InstantiationException e) {
				SimLogger.log(Level.SEVERE, "Cannot instantiate measure class with name = " + implClassName);
				throw new RuntimeException(e);
			} catch(IllegalAccessException e) {
				SimLogger.log(Level.SEVERE, "Cannot instantiate measure class with name = " + implClassName);
				throw new RuntimeException(e);
			}

			//init actors
			HashMap<String, Integer> actorMachineInstances = param.starter.buildActorMachine();
			for(String aType : actorMachineInstances.keySet()) {
				int numActors = actorMachineInstances.get(aType);
				SimLogger.log(Level.FINE, "Creating " + numActors + " many of actor type " + aType);
				for(int i = 0; i < numActors; i++) {
					try {
						Class actorClass = Class.forName(aType);
						ActorMachine am = (ActorMachine) actorClass.newInstance();
						String actorName = am.getPrefix() + i;
						am.actor = actorName;
						am.actorType = aType;
						am.sys = this;
						actors.put(actorName, am);
					} catch(ClassNotFoundException e) {
						SimLogger.log(Level.SEVERE, "Cannot find actor machine class with name = " + implClassName);
						e.printStackTrace();
					} catch(InstantiationException e) {
						SimLogger.log(Level.SEVERE, "Cannot instantiate actor machine class with name = " + implClassName);
						e.printStackTrace();
					} catch(IllegalAccessException e) {
						SimLogger.log(Level.SEVERE, "Cannot instantiate actor machine class with name = " + implClassName);
						e.printStackTrace();
					}
				}
			}
			SimLogger.log(Level.FINE, "Actors init was successful.");

		} catch(ClassNotFoundException e) {
			SimLogger.log(Level.SEVERE, "Cannot find implementation class with name = " + implClassName);
			e.printStackTrace();
		} catch(InstantiationException e) {
			SimLogger.log(Level.SEVERE, "Cannot instantiate implementation class with name = " + implClassName);
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			SimLogger.log(Level.SEVERE, "Cannot instantiate implementation class with name = " + implClassName);
			e.printStackTrace();
		}
	}

	void findMeasurables() {
		measurables.clear();
		measurables.add(new Property(this, Q_CURACTION));
		for(String actorName: actors.keySet()) {
			ActorMachine am = actors.get(actorName);
			for(Property p : am.getMeasurables()) {
				ActorProperty ap = (ActorProperty)p;
				ap.setActorName(actorName);
				measurables.add(ap);
			}
		}
		measurables.add(new Property(ActorMachine.class, "GetNames"));
		measurables.add(new Property(ActorMachine.class, "CurActor"));
		Workflow wa[] = workflows.toArray(new Workflow[0]);
		for(int i = 0; i < wa.length; i++) {
			for(Property p: wa[i].getMeasurables()) {
				p.params.add("" + i);
				measurables.add(p);
			}
		}
		measurables.add(new Property(Workflow.class, "GetItems"));
		measurables.add(new Property(Workflow.class, "GetCompletedItems"));
		for(Property p : impl.getMeasurables()) {
			measurables.add(p);
		}
	}

	@Override
	public Set<Property> getMeasurables() {
		return measurables;
	}

	@Override
	public boolean isMeasurable(Property p) {
		Set<Property> props = getMeasurables();
		for(Property pp : props) {
			if(pp.match(p)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getCurMeasure(Property p) throws InvalidMeasureException {
		SimLogger.log(Level.FINEST, "Looking at property " + p);
		if(p.qualifiers.contains(this.getClass().getName())) {
			if(p.name.equals(Q_CURACTION)) {
				return a;
			}
		}
		if(p instanceof ActorProperty) {
			// This is a property for a specific actor
			ActorProperty ap = (ActorProperty)p;
			String actor = ap.getActorName();
			return actors.get(actor).getCurMeasure(p);
		} else if(p.qualifiers.contains(ActorMachine.class.getName())) {
			if(p.name.equals("GetNames")) {
				return actors.keySet();
			} else if(p.name.equals("CurActor")) {
				return curActor;
			}
		} else if(p.qualifiers.contains(Workflow.class.getName())) {
			if(p.name.equals("GetItems")) {
				return workflows;
			}
			if(p.name.equals("GetCompletedItems")) {
				return completedWorkflows;
			}
			String index = p.params.get(p.params.size() - 1);
			p.params.remove(p.params.size() - 1);
			Integer i = Integer.parseInt(index);
			Iterator<Workflow> it = workflows.iterator();
			Workflow w = null;
			for(int j = 0; j <= i; j++) {
				w = it.next();
			}
			return w.getCurMeasure(p);
		} else if(impl.isMeasurable(p)) {
			return impl.getCurMeasure(p);
		}
		throw new InvalidMeasureException(p);
	}

	public void addActor(String name, ActorMachine am) {
		actors.put(name, am);
	}

	public void removeActor(String name) {
		actors.remove(name);
	}
}
