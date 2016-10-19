package gsis;

import java.util.HashSet;
import java.util.Set;
import java.util.Queue;
import java.util.LinkedList;

import base.Action;
import base.ActorMachine;
import base.ActorProperty;
import base.Conversion;
import base.InvalidMeasureException;
import base.Params;
import base.Property;
import base.SimLogger.Log;
import base.Simulation;
import gsis.PcPhases.Phase;
import gsis.PcPhases;

public class ActorNewPcReviewer extends ActorMachine {
	private double currentTime = 0;
	private Phase currentPhase = null;
	private Queue<Action> q = new LinkedList<Action>();
	private int reviewed = 0;

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
			return 0;
		}
		throw new InvalidMeasureException(p);
	}

	@Override
	public void transition(Action action) {
		// If there is something to do immediately, set it
		// if(action.name.equals("addO")) {
		// 	nextAction = "LAdd";
		// 	return;
		// }
		nextAction = null;
	}

	@Override
	public Action nextAction(Params params) {
		if(params.system.TIME_STOPPED) return null;

		currentTime = currentTime + params.delta;
		currentPhase = PcPhases.phase(currentTime);

		Action a = q.poll();
		if(a != null) {
			return a;
		}

		switch(currentPhase) {
			case Create:
				return createAction(params);
			case Recruit:
				return recruitAction(params);
			case Submit:
				return submitAction(params);
			case Review:
				return reviewAction(params);
			case Discuss:
				return discussAction(params);
			case Notify:
				return notifyAction(params);
		}

		return null;
	}

	private Action createAction(Params params) {
		return null;
	}

	private Action recruitAction(Params params) {
		return null;
	}

	private Action submitAction(Params params) {
		return null;
	}

	private Action reviewAction(Params params) {
		if(!params.reviewGroupsAdded) return null;
		if(reviewed >= 9) return null;

		double coin = Simulation.rand.nextDouble();
		double deltasLeft = PcPhases.phaseTimeLeft(currentTime) / params.delta - 20;
		if(deltasLeft < 1) deltasLeft = 1;

		if(coin <= 1.0/deltasLeft) {
			int rev = params.nextReview;
			params.nextReview += 1;

			q.add(new Action("LAdd", actor, "R" + rev, "X?"));

			reviewed += 1;
			Log.d("Reviewer " + actor + " is reviewing paper.");
			return new Action("addO", actor, "R" + rev);
		}

		return null;
	}

	private Action discussAction(Params params) {
		if(reviewed < 9) Log.e("Reviewer " + actor + " did not review all papers!");
		if(params.conflicted.contains(actor)) {
			Log.d("Reviewer " + actor + " is conflicted...");
			return null;
		}

		double coin = Simulation.rand.nextDouble() / params.delta;

		if(coin <= params.discussRate) {
			int msg = params.nextMessage;
			params.nextMessage += 1;

			Log.d("Reviewer " + actor + " discussing.");

			q.add(new Action("LAdd", actor, "M" + msg, "D"));

			return new Action("addO", actor, "M" + msg);
		}

		return null;
	}

	private Action notifyAction(Params params) {
		return null;
	}

	@Override
	public String getPrefix() {
		return "r";
	}

}

