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

public class ActorNewPcChair extends ActorMachine {
	private double currentTime = 0;
	private Phase currentPhase = null;
	private Queue<Action> q = new LinkedList<Action>();

	private boolean groupAdded = false;
	private boolean pcAdded = false;

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
		nextAction = null;
	}

	@Override
	public Action nextAction(Params params) {
		if(!params.system.TIME_STOPPED) {
			currentTime = currentTime + params.delta;
		}
		currentPhase = PcPhases.phase(currentTime);

		Action a = q.poll();
		if(a == null) {
			params.system.TIME_STOPPED = false;
		} else {
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
		if(!groupAdded) {
			groupAdded = true;
			Log.d("Chair creating D.");
			return new Action("addG", actor, "D");
		} else return null;
	}

	private Action recruitAction(Params params) {
		if(!pcAdded) {
			params.system.TIME_STOPPED = true;
			pcAdded = true;
			Log.d("Chair creating PC...");
			for(int i = 0; i < params.numReviewers; i++) {
				q.add(new Action("SJoin", actor, "r" + i, "D"));
			}
		}
		return null;
	}

	private Action submitAction(Params params) {
		return null;
	}

	private Action reviewAction(Params params) {
		// create a second group per paper, add random 3 pc
		if(!params.reviewGroupsAdded) {
			params.system.TIME_STOPPED = true;
			params.reviewGroupsAdded = true;
			Log.d("Chair creating review groups...");
			for(int i = 0; i < params.numAuthors; i++) {
				String rGroup = "X" + i;
				q.add(new Action("addG", actor, rGroup));
				q.add(new Action("SJoin", actor, "r?", rGroup));
				q.add(new Action("SJoin", actor, "r?", rGroup));
				q.add(new Action("SJoin", actor, "r?", rGroup));
				q.add(new Action("LAdd", actor, "P" + i, rGroup));
			}
		}
		return null;
	}

	private Action discussAction(Params params) {
		double coin = Simulation.rand.nextDouble() / params.delta;
		if(coin < params.rotateRate) {
			Log.d("Chair rotating paper from " + params.currentPaper);
			params.system.TIME_STOPPED = true;
			while(params.conflicted.size() > 0) {
				String guy = params.conflicted.poll();
				Log.d("Chair rejoins " + guy);
				q.add(new Action("SJoin", actor, guy, "D"));
			}
			params.currentPaper = Simulation.rand.nextInt(params.numAuthors);
			for(int i = 0; i < params.numReviewers; i++) {
				coin = Simulation.rand.nextDouble();
				if(coin < params.conflictRate) {
					Log.d("Chair sends away r" + i);
					q.add(new Action("LLeave", actor, "r" + i, "D"));
					params.conflicted.add("r" + i);
				}
			}
			q.add(new Action("LAdd", actor, "P" + params.currentPaper, "D"));
		}
		return null;
	}

	private Action notifyAction(Params params) {
		if(!params.summariesAdded) {
			params.system.TIME_STOPPED = true;
			params.summariesAdded = true;
			for(int i = 0; i < params.numAuthors; i++) {
				q.add(new Action("addO", actor, "S" + i));
				q.add(new Action("LAdd", actor, "S" + i, "A" + i));
			}
		}
		return null;
	}

	@Override
	public String getPrefix() {
		return "c";
	}

}

