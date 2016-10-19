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

/**Playstation Manager
 * Several times each month, several objects are liberalRemoved and are replaced
by others that are liberalAdded. These represent the free games. The number of
objects of this type thus remains constant.
Discounts also rotate fairly often (via strictRemove and liberalAdd), but the
number is not restricted to remain constant. More statistics are probably needed
here.
 * @author yechen
 *
 */
public class ActorSony extends ActorMachine {

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
	public Action nextAction(Params params) {
		double coin = Simulation.rand.nextDouble() / params.delta;
		double hoursInWeek = 168;
		double freeGameRefreshRate = 3 / hoursInWeek;
		double discountAddRate = 9 / hoursInWeek;
		double discountDelRate = 9 / hoursInWeek;

		if(nextAction != null) {
			String toExec = nextAction;
			nextAction = null;
			return new Action(toExec, actor);
		}

		if(coin < freeGameRefreshRate) {
			return new Action("LRemove", actor);
		}
		coin -= freeGameRefreshRate;

		if(coin < discountAddRate) {
			return new Action("LAdd", actor);
		}
		coin -= discountAddRate;

		if(coin < discountDelRate) {
			return new Action("SRemove", actor);
		}
		coin -= discountDelRate;

		return null;
	}

	@Override
	public void transition(Action action) {
		//LRemove -> LAdd (free games)
		if(action.name.equals("LRemove")) {
			nextAction = "LAdd";
			return;
		}

		nextAction = null;
	}

	@Override
	public String getPrefix() {
		return "a";
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

}
