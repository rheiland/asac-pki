package ac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import base.Scheme;
import base.State;
import base.Action;
import base.InvalidMeasureException;
import base.Property;
import base.SimLogger;

public class GsisPi extends Scheme {

	/**
	 * This map contains map between workflow action to GMS action.
	 */
	protected static Map<String, String> actorToGSIS = new HashMap<String, String>();

	public GsisPi(State initState) {
		super(initState);
		actorToGSIS.put("SAdd", "SAdd");
		actorToGSIS.put("SRemove", "SRemove");
		actorToGSIS.put("AddGroup", "AddGroup");
		actorToGSIS.put("SLeave", "SLeave");
		actorToGSIS.put("SJoin", "SJoin");
		actorToGSIS.put("AddObject", "AddObject");
		actorToGSIS.put("Auth", "Auth");
		actorToGSIS.put("LAdd", "LAdd");
		actorToGSIS.put("LRemove", "LRemove");
		actorToGSIS.put("LLeave", "LLeave");
		actorToGSIS.put("LJoin", "LJoin");
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
		case "AddObject":
			s.addObject(action.params[1]);
			break;
		case "SAdd":
			//object, group
			s.addStrictAdd(action.params[1], action.params[2]);
			break;
		case "SRemove":
			s.addStrictRemove(action.params[1], action.params[2]);
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
		"SAdd", "SRemove", "AddGroup", "SLeave", "SJoin", "Auth", "AddObject", "LAdd", "LRemove", "LLeave", "LJoin",
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
		if(p.name.equals(Scheme.Q_ACTIONS)) {
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
		case Scheme.Q_ACTIONS:
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

}
