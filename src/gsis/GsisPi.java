package gsis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.logging.Level;

import base.Action;
import base.IAccessW;
import base.InvalidMeasureException;
import base.Property;
import base.SimLogger;
import base.Simulation;
import base.Workflow;
import base.WorkloadScheme;
import base.WorkloadState;

public class GsisPi extends WorkloadScheme {

	/**
	 * This map contains map between workflow action to GMS action.
	 */
	protected static Map<String, String> actorToGSIS = new HashMap<String, String>();

	public GsisPi(WorkloadState initState) {
		super(initState);
		actorToGSIS.put("SAdd", "SAdd");
		actorToGSIS.put("SRemove", "SRemove");
		actorToGSIS.put("SLeave", "SLeave");
		actorToGSIS.put("SJoin", "SJoin");
		actorToGSIS.put("auth", "auth");
		actorToGSIS.put("LAdd", "LAdd");
		actorToGSIS.put("LRemove", "LRemove");
		actorToGSIS.put("LLeave", "LLeave");
		actorToGSIS.put("LJoin", "LJoin");
		actorToGSIS.put("addG", "addG");
		actorToGSIS.put("addO", "addO");
		actorToGSIS.put("delG", "delG");
		actorToGSIS.put("delO", "delO");
	}

	public void command(Action action) {
		GsisPiState s = (GsisPiState) state;
		if(!actorToGSIS.containsKey(action.name)) {
			SimLogger.log(Level.FINEST, getClass().getName() + " Workflow Action: " + action.name + " cannot be executed in GMS.");
			return;
		} else {
			SimLogger.log(Level.FINEST, getClass().getName() + " Workflow Action: " + action.name + " is resolved as " +
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
		case "LRemove":
			s.addLiberalRemove(action.params[1], action.params[2]);
		case "LLeave":
			//subject, group
			s.addLiberalLeave(action.params[1], action.params[2]);
			break;
		case "LJoin":
			//subject, group
			s.addLiberalJoin(action.params[1], action.params[2]);
			break;
		case "SAdd":
			//object, group
			s.addStrictAdd(action.params[1], action.params[2]);
			break;
		case "SRemove":
			s.addStrictRemove(action.params[1], action.params[2]);
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
		GsisPiState s = (GsisPiState) state;
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
		case "Add": {
			OGPair pair = new OGPair(action.params[1], action.params[2]);
			List<Integer> i = s.getAddTimes(pair);
			if(i.contains(Integer.parseInt(action.params[3]))) {
				lastQueryResult = true;
				return;
			}
			lastQueryResult = false;
			return;
		}
		case "Remove": {
			OGPair pair = new OGPair(action.params[1], action.params[2]);
			List<Integer> i = s.getRemoveTimes(pair);
			if(i.contains(Integer.parseInt(action.params[3]))) {
				lastQueryResult = true;
				return;
			}
			lastQueryResult = false;
			return;
		}
		case "authForward": {
			SGPair sg = new SGPair(action.params[1], action.params[3]);
			OGPair og = new OGPair(action.params[2], action.params[3]);
			List<Integer> joins = s.getJoinTimes(sg);
			List<Integer> adds = s.getAddTimes(og);
			for(Integer t1: joins) {
				for(Integer t2: adds) {
					if(t2 > t1) {
						//forall t3: Leave(s, g, t3) => (t1 > t3 or t3 > t2)
						List<Integer> leaves = s.getLeaveTimes(sg);
						boolean test = true;
						for(Integer t3 : leaves) {
							//negation of RHS
							if(t3 >= t1 && t2 >= t3) {
								test = false;
								continue;
							}
						}

						leaves = s.getStrictLeaves(sg);
						for(Integer t3 : leaves) {
							if(t3 >= t2) {
								test = false;
								continue;
							}
						}

						List<Integer> removes = s.getStrictRemoves(og);
						for(Integer t3 : removes) {
							if(t3 >= t2) {
								test = false;
								continue;
							}
						}

						//None of these happened, this means we're done
						if(test) {
							lastQueryResult = true;
							return;
						}
					}
				}
			}
			lastQueryResult = false;
			return;
		}
		case "authBackward": {
			SGPair sg = new SGPair(action.params[1], action.params[3]);
			OGPair og = new OGPair(action.params[2], action.params[3]);
			List<Integer> joins = s.getLiberalJoins(sg);
			List<Integer> adds = s.getLiberalAdds(og);
			for(Integer t1 : joins) {
				for(Integer t2: adds) {
					if(t1 > t2) {
						boolean test = true;
						List<Integer> remove = s.getRemoveTimes(og);
						for(Integer t3 : remove) {
							if(t3 >= t2 && t1 >= t3) {
								test = false;
								continue;
							}
						}

						remove = s.getStrictRemoves(og);
						for(Integer t3 : remove) {
							if(t3 >= t1) {
								test = false;
								continue;
							}
						}

						List<Integer> leave = s.getStrictLeaves(sg);
						for(Integer t3 : leave) {
							if(t3 >= t1) {
								test = false;
								continue;
							}
						}

						//None of these happened, this means we're done
						if(test) {
							lastQueryResult = true;
							return;
						}
					}
				}
			}
			lastQueryResult = false;
			return;
		}
		case "auth":
			Action a = new Action(true, "authBackward", action.params);
			query(a);
			if(lastQueryResult) {
				return;
			}
			a = new Action(true, "authForward", action.params);
			query(a);
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
		"SAdd", "SRemove", "SLeave", "SJoin", "auth", "LAdd", "LRemove", "LLeave", "LJoin",
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
		String u = a.params[0];
		String obj, group;

		//In this admin-free model, users can join any group they want, but cannot
		//make other users join or leave a group.
		switch(a.name) {
		case "addO":
			objCounter++;
			return new Action(a.name, u, "m" + (objCounter));
		case "addG":
			groupCounter++;
			return new Action(a.name, u, "g" + (groupCounter));
		case "delO":
			String o = randomObject();
			if(o == null) return null;
			return new Action(a.name, u, o);
		case "delG":
			String g = randomGroup();
			if(g == null) return null;
			return new Action(a.name, u, g);
		case "SAdd":
			group = randomGroup();
			if(group == null) return null;
			obj = randomObject();
			if(obj == null) return null;
			return new Action(a.name, u, obj, group);
		case "SRemove":
			group = randomGroup();
			obj = randomObject();
			if(group == null) return null;
			if(obj == null) return null;
			objRemoved.add(obj);
			return new Action(a.name, u, obj, group);
		case "SLeave":
			group = randomGroup();
			if(group == null) return null;
			return new Action(a.name, u, u, group);
		case "SJoin":
			group = randomUnoccupiedGroup(u);
			if(group == null) return null;
			return new Action(a.name, u, u, group);
		case "LAdd":
			group = randomGroup();
			if(group == null) return null;
			obj = randomObject();
			if(obj == null) return null;
			return new Action(a.name, u, obj, group);
		case "LRemove":
			group = randomGroup();
			obj = randomObject();
			if(group == null) return null;
			if(obj == null) return null;
			objRemoved.add(obj);
			return new Action(a.name, u, obj, group);
		case "LLeave":
			group = randomGroup();
			if(group == null) return null;
			return new Action(a.name, u, u, group);
		case "LJoin":
			group = randomUnoccupiedGroup(u);
			if(group == null) return null;
			return new Action(a.name, u, u, group);
		case "auth":
			group = randomGroup();
			obj = randomObject();
			if(group == null) return null;
			if(obj == null) return null;
			return new Action(a.name, u, u, obj, group);

		default:
			SimLogger.log(Level.WARNING, "Cannot add parameter for action" + a.name);
		}
		return null;
	}

	/**
	 * Return a random group that the user is NOT currently in.
	 * @param u the user to check for
	 * @return
	 */
	private String randomUnoccupiedGroup(String u) {
		ArrayList<String> ret = new ArrayList<String>();
		GsisPiState gState = (GsisPiState) state;
		IAccessW<String> it = gState.getG();
		while(it.hasNext()) {
			String group = it.next();
			SGPair test = new SGPair(u, group);
			if(!groupRemoved.contains(group));
			ret.add(group);
			for(Integer jointime : gState.getJoinTimes(test)) {
				//join only counts now if we don't have a leave after
				boolean toDel = true;
				for(Integer leavetime : gState.getLeaveTimes(test)) {
					if(leavetime > jointime) {
						toDel = false;
						break;
					}
				}
				if(toDel) {
					ret.remove(group);
				}
			}
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}


	private String randomGroup() {
		ArrayList<String> ret = new ArrayList<String>();
		GsisPiState gState = (GsisPiState) state;
		IAccessW<String> it = gState.getG();
		while(it.hasNext()) {
			String group = it.next();
			if(!groupRemoved.contains(group))
				ret.add(group);
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}

	private String randomObject() {
		ArrayList<String> ret = new ArrayList<String>();
		GsisRoleLikeState gState = (GsisRoleLikeState) state;
		IAccessW<String> it = gState.getO();
		while(it.hasNext()) {
			String obj = it.next();
			if(!objRemoved.contains(obj))
				ret.add(obj);
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}

	private String randomSubject() {
		ArrayList<String> ret = new ArrayList<String>();
		GsisPiState gState = (GsisPiState) state;
		IAccessW<String> it = gState.getS();
		while(it.hasNext()) {
			String obj = it.next();
			ret.add(obj);
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}

}
