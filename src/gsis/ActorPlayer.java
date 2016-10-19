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

/**
 * PSP: Actor-Player autonomous join/leave
 * @author yechen
 *
 */
public class ActorPlayer extends ActorMachine {

	private boolean pspMember = false;
	private int memberShipExpires = 0;

	private void becomeMember() {
		pspMember = true;
		//delta in hours
		memberShipExpires = 24 * 30 * 3;
	}

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
		double coin = Simulation.rand.nextDouble();
		coin = coin / params.delta;
		double hoursInWeek = 168;

		double joinRate = 1 / hoursInWeek; //1 per week
		double authRate = 0;
		//double authRate = 7 / secondsInWeek;

		//leave after membership expires (deterministic)
		if(pspMember) {
			memberShipExpires -= params.delta;
			if(memberShipExpires < 0) {
				pspMember = false;
				return new Action("SLeave", actor);
			}
		}

		if(coin < joinRate) {
			if(!pspMember) {
				becomeMember();
				return new Action("LJoin", actor);
			} else return null;
		}
		coin -= joinRate;

		if(coin < authRate) {
			return new Action("auth", actor);
		}

		return null;
	}

	@Override
	public String getPrefix() {
		return "u";
	}

}
