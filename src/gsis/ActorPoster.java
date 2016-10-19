package gsis;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import base.Action;
import base.ActorMachine;
import base.ActorProperty;
import base.InvalidMeasureException;
import base.Params;
import base.Property;
import base.SimLogger;
import base.Simulation;
import base.WorkloadScheme;

public class ActorPoster extends ActorMachine {

	@Override
	public String getPrefix() {
		return "p";
	}

	@Override
	public Set<Property> getMeasurables() {
		Set<Property> ret = new HashSet<Property>();
		ret.add(new ActorProperty(this, Q_TIME, new String[0]));
		return ret;
	}
	@Override
	public boolean isMeasurable(Property p) {
		Property qTest = new ActorProperty(this, "__test", new String[0]);
		if(!qTest.matchQualifier(p)) {
			return false;
		}
		if(p.name.equals(Q_TIME)) return true;
		return false;
	}
	@Override
	public Object getCurMeasure(Property p) {
		Property qTest = new ActorProperty(this, "__test", new String[0]);
		if(!qTest.matchQualifier(p)) {
			throw new InvalidMeasureException(p);
		}
		if(p.name.equals(Q_TIME)) {
			//actor takes some time to add object/read object.
			if(p.params.get(0).contains("Add")) {
				return 2;
			}
			if(p.params.get(0).equals("auth")) {
				return 2;
			}
			//managements are easy for this actor.
			return 1;
		}
		throw new InvalidMeasureException(p);
	}

	@Override
	public Action nextAction(Params params) {
		double coin = Simulation.rand.nextDouble() / params.delta;
		double post_freqm = params.post_freqm, churn_factor = params.churn_factor,
		       del_factor = params.del_factor;
		if(nextAction != null) {
			if(coin < nextRate) {
				String toExec = nextAction;
				nextAction = null;
				return new Action(toExec, actor);
			}
		}

		//How often would the manager join/leave a user or create a new group?
		//dT = 1 hour
		double thresholdJoin = churn_factor / 24; //1/2 per day by default
		double thresholdLeave = 0.5 * churn_factor / 24; //0 per day by default.
		double thresholdGroupAdd = 0; //1 per day by default
		//double thresholdDeletion = 0;
		double thresholdRemove = 1 * del_factor / 24; //3 per day
		double thresholdGroupDeletion = 0; //1/4 per day by default
		double thresholdObjectDeletion = 0;
		double thresholdPost = 3 * post_freqm / 24; //1 per day by default.
		double thresholdaddO = 1 * post_freqm / 24; //1 per day by default.
		double thresholdAuth = 0;
		//double thresholdAuth = 0.3 * params.delta;
		nextRate = thresholdPost * params.create_post_factor;

		String[] actions = (String[]) sys.getCurMeasure(new Property(
		                           WorkloadScheme.class, WorkloadScheme.Q_ACTIONS, new String[0]));

		if(coin < thresholdaddO) {
			return new Action("addO", actor);
		}
		coin -= thresholdaddO;

		if(coin <  thresholdPost) {
			double subcoin = coin / thresholdPost;
			HashSet<String> possiblePostActions = new HashSet<String>();
			for(String a : actions) {
				if(a.endsWith("Add")) {
					possiblePostActions.add(a);
				}
			}
			double testThreshold = 1.0 / possiblePostActions.size();

			for(String a : possiblePostActions) {
				if(subcoin < testThreshold) {
					return new Action(a, actor);
				}
				subcoin -= testThreshold;
			}
			return null;
		}
		coin -= thresholdPost;

		if(coin < thresholdJoin) {
			double subcoin = coin / thresholdJoin;
			HashSet<String> possibleManagementActions = new HashSet<String>();
			for(String a : actions) {
				if(a.contains("Join")) {
					possibleManagementActions.add(a);
				}
			}
			double testThreshold = 1.0 / possibleManagementActions.size();

			for(String a : possibleManagementActions) {
				if(subcoin < testThreshold) {
					return new Action(a, actor);
				}
				subcoin -= testThreshold;
			}
			SimLogger.log(Level.WARNING, getClass().getName() + ": Possible actor machine probability miscalculation" +
			              " in management");
			return null;
		}
		coin -= thresholdJoin;

		if(coin < thresholdLeave) {
			double subcoin = coin / thresholdLeave;
			HashSet<String> possibleManagementActions = new HashSet<String>();
			for(String a : actions) {
				if(a.contains("Leave")) {
					possibleManagementActions.add(a);
				}
			}
			double testThreshold = 1.0 / possibleManagementActions.size();

			for(String a : possibleManagementActions) {
				if(subcoin < testThreshold) {
					return new Action(a, actor);
				}
				subcoin -= testThreshold;
			}
			SimLogger.log(Level.WARNING, getClass().getName() + ": Possible actor machine probability miscalculation" +
			              " in management");
			return null;
		}
		coin -= thresholdLeave;

		if(coin < thresholdGroupAdd) {
			return new Action("addG", actor);
		}
		coin -= thresholdGroupAdd;

		if(coin < thresholdGroupDeletion) {
			return new Action("delG", actor);
		}
		coin -= thresholdGroupDeletion;

		if(coin < thresholdObjectDeletion) {
			return new Action("delO", actor);
		}
		coin -= thresholdObjectDeletion;

		if(coin < thresholdRemove) {
			double subcoin = coin / thresholdRemove;
			HashSet<String> possibleDelActions = new HashSet<String>();
			for(String a : actions) {
				if(a.endsWith("Remove")) {
					possibleDelActions.add(a);
				}
			}
			double testThreshold = 1.0 / possibleDelActions.size();

			for(String a : possibleDelActions) {
				if(subcoin < testThreshold) {
					return new Action(a, actor);
				}
				subcoin -= testThreshold;
			}

			SimLogger.log(Level.WARNING, getClass().getName() + ": Possible actor machine probability miscalculation" +
			              " in deletion");
			return null;
		}
		coin -= thresholdRemove;


		if(coin < thresholdAuth) {
			return new Action("auth", actor);
		}

		return null;
	}

	public void transition(Action action) {
		String[] actions = (String[]) sys.getCurMeasure(new Property(
		                           WorkloadScheme.class, WorkloadScheme.Q_ACTIONS, new String[0]));
		if(action.name.endsWith("Join")) {
			double subcoin = Simulation.rand.nextDouble();
			HashSet<String> possiblePostActions = new HashSet<String>();
			for(String a : actions) {
				if(a.toLowerCase().endsWith("add")) {
					possiblePostActions.add(a);
				}
			}
			double testThreshold = 1.0 / possiblePostActions.size();

			for(String a : possiblePostActions) {
				if(subcoin < testThreshold) {
					nextAction = a;
					return;
				}
				subcoin -= testThreshold;
			}
		}

		if(action.name.equals("addO")) {
			double subcoin = Simulation.rand.nextDouble();
			HashSet<String> possiblePostActions = new HashSet<String>();
			for(String a : actions) {
				if(a.endsWith("Add")) {
					possiblePostActions.add(a);
				}
			}
			double testThreshold = 1.0 / possiblePostActions.size();

			for(String a : possiblePostActions) {
				if(subcoin < testThreshold) {
					nextAction = a;
					return;
				}
				subcoin -= testThreshold;
			}
		}

	}

}
