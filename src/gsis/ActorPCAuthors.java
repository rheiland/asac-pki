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

public class ActorPCAuthors extends ActorMachine {

	private static final int CREATE_PERIOD_LENGTH = 2 * 30;
	private static final int JOIN_PERIOD_LENGTH = 2 *  30;
	private static final int REVIEW_PERIOD_LENGTH = 4 * 30;
	private int curPeriod = 0;
	private int numCOILeaves = 0;

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
	public void transition(Action action) {
		if(action.name.equals("addO")) {
			nextAction = "LAdd";
			return;
		}

		nextAction = null;
	}

	@Override
	public Action nextAction(Params params) {
		curPeriod = (curPeriod + (int) params.delta) % (
				CREATE_PERIOD_LENGTH + JOIN_PERIOD_LENGTH + REVIEW_PERIOD_LENGTH);
		double coin = Simulation.rand.nextDouble() / params.delta;
		double daysInYear = 365;
		double daysInWeek = 7;
		double addGroupRate = 0.5 / daysInYear; //adding group //0.5 per year
		double delGroupRate = 0.01 / daysInYear; //deleting group
		double joinRate = 0.5 / daysInWeek; // only in join period
		double leaveRate = 0.25 / daysInWeek; //COI leave, only active in review time.
		double sLeaveRate = 0.5 / daysInYear;
		double sJoinRate = 0.25 / daysInWeek; //COI join, only active in review time.
		double addPaperObjRate = 1.0 / 30;
		double addReviewObjRate = 1.0 / daysInWeek;

		if(nextAction != null) {
			String toExec = nextAction;
			nextAction = null;
			return new Action(toExec, actor);
		}

		if(curPeriod < CREATE_PERIOD_LENGTH) {
			if(coin < addGroupRate) {
				return new Action("addG", actor);
			}
			coin -= addGroupRate;

			if(coin < delGroupRate) {
				return new Action("delG", actor);
			}
			coin -= delGroupRate;
		}

		if(curPeriod < JOIN_PERIOD_LENGTH) {

			//join period, liberaljoin, add paper, liberal add
			if(coin < joinRate) {
				return new Action("LJoin", actor);
			}
			coin -= joinRate;

			if(coin < addPaperObjRate) {
				return new Action("addO", actor);
			}
			coin -= addPaperObjRate;

			if(coin < sLeaveRate) {
				return new Action("SLeave", actor);
			}
			coin -= sLeaveRate;

			if(coin < delGroupRate) {
				return new Action("delG", actor);
			}
			coin -= delGroupRate;
		} else {
			//review period, liberal leave
			//liberal add

			//deterministi rejoin COI LLeaves
			if(coin < sJoinRate) {
				if(numCOILeaves > 0) {
					numCOILeaves--;
					return new Action("SJoin", actor);
				}
			}
			coin -= sJoinRate;

			if(coin < leaveRate) {
				numCOILeaves += 1;
				return new Action("LLeave", actor);
			}
			coin -= leaveRate;

			if(coin < addReviewObjRate) {
				return new Action("addO", actor);
			}
			coin -= addReviewObjRate;
		}
		return null;
	}

	@Override
	public String getPrefix() {
		return "u";
	}

}
