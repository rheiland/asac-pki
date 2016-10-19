package gsis;

import java.util.HashSet;
import java.util.Set;

import base.Action;
import base.ActorMachine;
import base.ActorProperty;
import base.InvalidMeasureException;
import base.Params;
import base.Property;
import base.Simulation;
import base.WorkloadScheme;

public class ActorReader extends ActorMachine {

	@Override
	public String getPrefix() {
		return "c";
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

		double thresholdAuth = 0.3 * params.delta;

		String[] actions = (String[]) sys.getCurMeasure(new Property(
		                           WorkloadScheme.class, WorkloadScheme.Q_ACTIONS, new String[0]));

		if(coin < thresholdAuth) {
			return new Action("auth", actor);
		}

		return null;
	}

}
