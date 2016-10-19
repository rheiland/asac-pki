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

public class ActorNewPcAuthor extends ActorMachine {
	private double currentTime = 0;
	private Phase currentPhase = null;
	private Queue<Action> q = new LinkedList<Action>();

	private boolean submitted = false;

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
			// print something when the LAdd actually happens
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
		if(submitted) return null;

		double coin = Simulation.rand.nextDouble();
		double deltasLeft = PcPhases.phaseTimeLeft(currentTime) / params.delta - 3;
		if(deltasLeft < 0) {
			Log.w("Author " + actor + " ran out of time to submit paper.");
			return null;
		} else if(coin <= 1.0/deltasLeft) {
			String whoami = actor.substring(1);
			submitted = true;
			q.add(new Action("addO", actor, "P" + whoami));
			q.add(new Action("LAdd", actor, "P" + whoami, "A" + whoami));
			Log.d("Author " + actor + " is submitting paper.");
			return new Action("addG", actor, "A" + whoami);
		} else {
			return null;
		}
	}

	private Action reviewAction(Params params) {
		return null;
	}

	private Action discussAction(Params params) {
		return null;
	}

	private Action notifyAction(Params params) {
		if(!params.summariesAdded) return null;

		double coin = Simulation.rand.nextDouble();
		double deltasElapsed = PcPhases.phaseTimeElapsed(currentTime) / params.delta + 2;
		if(coin <= 1.0/deltasElapsed) {
			String whoami = actor.substring(1);
			Log.d("Author " + actor + " is reading notification.");
			return new Action("auth", actor, "S" + whoami, "A" + whoami);
		}
		return null;
	}

	@Override
	public String getPrefix() {
		return "a";
	}

}

