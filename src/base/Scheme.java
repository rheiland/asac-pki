package base;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This scheme represents the scheme to be simulated in.
 *
 * Requires subclass implementaion of:
 * 	command(Action): tells AC what to do with a given AC action.
 * 	Note: AC action are most likely different from workload action, the mapping is resolved through
 * 	implementation.
 *
 * All subclass constructors must be of the form Scheme(State) and must call:
 * 	super(initState)
 * @author yechen
 *
 */
public abstract class Scheme implements Measurable {
	/**
	 * The AC state that stores all the data.
	 */
	public State state;
	private State prevState;
	public Boolean lastQueryResult = new Boolean(false);
	/**
	 * What actions I've executed for a given workload action so far.
	 * May be useful for something like stutter cost.
	 */
	ArrayList<Action> actionsExecuted = new ArrayList<Action>();

	//measure queries
	public static final String Q_LASTSTATE="LastState", Q_ACTIONSEXECUTED = "ActionsExecuted", Q_ACTIONS = "ActionList",
	                           Q_CLASSNAME="ClassName", Q_LASTQUERYRESULT = "LastQueryResult";

	// common properties
	public static final Property actionsProperty = new Property(Scheme.class, Scheme.Q_ACTIONSEXECUTED);

	public boolean noReset = false;
	/**
	 * Given an acAtion, execute the action.
	 * @param acAction the AC action to be executed.
	 */
	public void action(Action acAction) {
		prevState = state.createCopy();
		actionsExecuted.add(acAction);
		prevState.accessR = state.accessR;
		prevState.accessW = state.accessW;
	}

	/**
	 * I started to execute a new workload command.
	 * This means I should clear actionExecuted so stutter is measured correctly.
	 * Also, I/O cost should have been aggregated already and should be cleared to 0.
	 */
	public void newWorkloadCommand() {
		if(noReset) return;
		actionsExecuted.clear();
		state.newWorkloadCommand();
	}

	/**
	 * Constructor of the AC Scheme takes in the initial AC state as parameter.
	 * Implementation's init() method should construct the AC state given a workload state.
	 * @param initState
	 */
	public Scheme(State initState) {
		state = initState;
	}

	@Override
	public Set<Property> getMeasurables() {
		HashSet<Property> ret = new HashSet<Property>();
		ret.add(new Property(this, Q_LASTSTATE));
		ret.add(new Property(this, Q_ACTIONSEXECUTED));
		ret.add(new Property(this, Q_CLASSNAME));
		ret.add(new Property(this, Q_LASTQUERYRESULT));
		ret.addAll(state.getMeasurables());
		return ret;
	}

	@Override
	public boolean isMeasurable(Property p) {
		Property qTest = new Property(this, "__test");
		if(!qTest.matchQualifier(p)) {
			qTest = new Property(state.getClass(),  "__test");
			if(!qTest.matchQualifier(p)) {
				return false;
			}
		}
		Set<Property> props = getMeasurables();
		for(Property pp : props) {
			if(pp.match(p)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getCurMeasure(Property p) {
		if(state.isMeasurable(p)) {
			return state.getCurMeasure(p);
		}
		Property qTest = new Property(this, "__test");
		if(!qTest.matchQualifier(p)) {
			throw new InvalidMeasureException(p);
		}
		if(p.name.equals(Q_LASTSTATE)) {
			return prevState;
		}
		if(p.name.equals(Q_ACTIONSEXECUTED)) {
			return actionsExecuted;
		}
		if(p.name.equals(Q_CLASSNAME)) {
			return this.getClass().getName();
		}
		if(p.name.equals(Q_LASTQUERYRESULT)) {
			return lastQueryResult;
		}
		throw new InvalidMeasureException(p);
	}

}
