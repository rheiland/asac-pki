package gsis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Collections;
import java.util.logging.Level;

import base.Action;
import base.IAccessW;
import base.InvalidMeasureException;
import base.Property;
import base.SimLogger.Log;
import base.Simulation;
import base.Workflow;
import base.WorkloadScheme;
import base.WorkloadState;

public class GsisPc extends WorkloadScheme {

	/**
	 * This map contains map between workflow action to GMS action.
	 */
	protected static Map<String, String> actorToGSIS = new HashMap<String, String>();

	// map from reviewer to review groups joined
	private Map<String, LinkedList<String>> reviewGroups = new HashMap<String, LinkedList<String>>();
	private List<String> availRevs = null;

	public GsisPc(WorkloadState initState) {
		super(initState);
		actorToGSIS.put("SLeave", "SLeave");
		actorToGSIS.put("SJoin", "SJoin");
		actorToGSIS.put("auth", "auth");
		actorToGSIS.put("LAdd", "LAdd");
		actorToGSIS.put("LLeave", "LLeave");
		actorToGSIS.put("LJoin", "LJoin");
		actorToGSIS.put("addG", "addG");
		actorToGSIS.put("addO", "addO");
		actorToGSIS.put("delG", "delG");
		actorToGSIS.put("delO", "delO");
	}

	public void command(Action action) {
		GsisPcState s = (GsisPcState) state;
		if(!actorToGSIS.containsKey(action.name)) {
			Log.e(getClass().getName() + " Workflow Action: " + action.name + " cannot be executed in GMS.");
			return;
		} else {
			Log.i(getClass().getName() + " Workflow Action: " + action.name + " is resolved as " +
			              actorToGSIS.get(action.name));
		}
		switch(actorToGSIS.get(action.name)) {
		case "delO":
			s.delObject(action.params[1]);
			break;
		case "delG":
			s.delGroup(action.params[1]);
			break;
		case "addO":
			s.addObject(action.params[1]);
			break;
		case "addG":
			s.addGroup(action.params[1]);
			break;
		case "LAdd":
			//object, group
			s.addLiberalAdd(action.params[1], action.params[2]);
			break;
		case "LLeave":
			//subject, group
			s.addLiberalLeave(action.params[1], action.params[2]);
			break;
		case "LJoin":
			//subject, group
			s.addLiberalJoin(action.params[1], action.params[2]);
			break;
		case "AddObject":
			s.addObject(action.params[1]);
			break;
		case "AddGroup":
			s.addGroup(action.params[1]);
			break;
		case "SLeave":
			//subject, group
			s.addStrictLeave(action.params[1], action.params[2]);
			break;
		case "SJoin":
			//subject, group
			s.addStrictJoin(action.params[1], action.params[2]);
			break;
		}
	}

	public void query(Action action) {
		GsisPcState s = (GsisPcState) state;
		switch(action.name) {
		case "Join": {
			SGPair pair = new SGPair(action.params[1], action.params[2]);
			List<Integer> i = s.getJoinTimes(pair);
			if(i.contains(Integer.parseInt(action.params[3]))) {
				lastQueryResult = true;
				return;
			}
			lastQueryResult = false;
			return;
		}
		case "Leave": {
			SGPair pair = new SGPair(action.params[1], action.params[2]);
			List<Integer> i = s.getLeaveTimes(pair);
			if(i.contains(Integer.parseInt(action.params[3]))) {
				lastQueryResult = true;
				return;
			}
			lastQueryResult = false;
			return;
		}
		case "auth":
			// Cripple until we can do it right
			Log.v("" + action.params);
			SGPair sg = new SGPair(action.params[0], action.params[2]);
			OGPair og = new OGPair(action.params[1], action.params[2]);
			s.getJoinTimes(sg);
			s.getLeaveTimes(sg);
			s.getLiberalAdds(og);
			return;
		}
	}

	@Override
	public void action(Action action) {
		super.action(action);
		objRemoved.clear();
		groupRemoved.clear();
		if(!action.isQuery) {
			command(action);
		} else {
			query(action);
		}
	}

	public static final String[] AVAIL_ACTIONS = {
		"addO", "delO", "delG", "addG",
		"SLeave", "SJoin", "auth", "LAdd", "LLeave", "LJoin"
	};

	@Override
	public boolean isMeasurable(Property p) {
		if(super.isMeasurable(p)) {
			return true;
		}
		Property qTest = new Property(this, "__test", new String[0]);
		if(!qTest.matchQualifier(p)) {
			return false;
		}
		if(p.name.equals(WorkloadScheme.Q_ACTIONS)) {
			return true;
		}
		return false;
	}

	@Override
	public Object getCurMeasure(Property p) {
		if(super.isMeasurable(p)) {
			return super.getCurMeasure(p);
		}
		Property qTest = new Property(this, "__test", new String[0]);
		if(!qTest.matchQualifier(p)) {
			throw new InvalidMeasureException(p);
		}
		switch(p.name) {
		case WorkloadScheme.Q_ACTIONS:
			return AVAIL_ACTIONS;
		}
		throw new InvalidMeasureException(p);
	}

	@Override
	public Set<Property> getMeasurables() {
		HashSet<Property> ret = new HashSet<Property>();
		ret.addAll(super.getMeasurables());
		ret.add(new Property(this, Q_ACTIONS, new String[0]));
		return ret;
	}

	protected int objCounter = 0, groupCounter = 0;

	private HashSet<String> objRemoved = new HashSet<String>();
	private HashSet<String> groupRemoved = new HashSet<String>();

	@Override
	public Action addParameters(Action a, Workflow w) {
		if(a.name.equals("LAdd") && a.params[2].equals("X?")) {
			String theRev = a.params[0];

			Log.d("Filling parameter X? for " + theRev + " from " + reviewGroups.get(theRev));
			String theGroup = reviewGroups.get(theRev).poll();

			if(theGroup == null) {
				Log.w("Ran out of review??");
				return a;
			}

			Log.d("Filling parameter X? with " + theGroup + " " + reviewGroups.get(theRev));
			return new Action(a.name, a.params[0], a.params[1], theGroup);
		}
		if(a.name.equals("SJoin") && a.params[1].equals("r?")) {
			if(availRevs == null) {
				Log.d("Creating reviewers list.");
				availRevs = new LinkedList<String>();
				GsisPcState gState = (GsisPcState)state;
				IAccessW<String> it = gState.getS();
				while(it.hasNext()) {
					String actor = it.next();
					if(actor.startsWith("r")) availRevs.add(actor);
				}
				Log.d("availRevs: " + availRevs);
			}

			String theGroup = a.params[2];
			String theRev = "";
			int counter = 0;

			// find a valid reviewer
			while(theRev.equals("")) {
				// get a random reviewer
				Collections.shuffle(availRevs, Simulation.rand);
				theRev = availRevs.get(0);
				// if the reviewer isn't in map yet, add it
				if(!reviewGroups.containsKey(theRev)) {
					reviewGroups.put(theRev, new LinkedList<String>());
				}
				// if the reviewer has all reviews, remove it
				// and try again
				if(reviewGroups.get(theRev).size() >= 9) {
					Log.i("Reviewer " + theRev + " is assigned all reviews.");
					availRevs.remove(theRev);
                    Log.d("availRevs: " + availRevs);
					theRev = "";
					continue;
				}
				if(counter >= 10) {
					Log.w("Getting stuck, trying to swap from " + theRev);
					String mine = reviewGroups.get(theRev).getLast();
					String swapper = "";
					String yours = mine;
					while(reviewGroups.get(theRev).contains(yours) || reviewGroups.get(swapper).contains(mine)) {
						swapper = "r" + Simulation.rand.nextInt(reviewGroups.size());
						yours = reviewGroups.get(swapper).getLast();
					}
					Log.w("Swapping with " + swapper);
					reviewGroups.get(theRev).remove(mine);
					reviewGroups.get(theRev).add(yours);
					reviewGroups.get(swapper).remove(yours);
					reviewGroups.get(swapper).add(mine);

					GsisPcState s = (GsisPcState)state;
					s.addStrictLeave(swapper, yours);
					s.addLiberalJoin(swapper, mine);
					s.addStrictLeave(theRev, mine);
					s.addLiberalJoin(theRev, yours);
				}
				if(reviewGroups.get(theRev).contains(theGroup)) {
					counter++;
					theRev = "";
					continue;
				}
			}
			// add to reviewer's groups
			reviewGroups.get(theRev).add(theGroup);

			Log.d("Filling parameter r? with " + theRev + " " + reviewGroups.get(theRev) + " for " + theGroup);
			return new Action(a.name, a.params[0], theRev, a.params[2]);
		}
		return a;
	}

}
