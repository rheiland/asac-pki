package gsis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

public class GsisTop extends WorkloadScheme {

	public GsisTop(WorkloadState initState) {
		super(initState);
		actorToGSIS.put("SAdd", "SAdd");
		actorToGSIS.put("SRemove", "SRemove");
		actorToGSIS.put("SLeave", "SLeave");
		actorToGSIS.put("SJoin", "SJoin");
		actorToGSIS.put("auth", "auth");
		actorToGSIS.put("addG", "addG");
		actorToGSIS.put("addO", "addO");
		actorToGSIS.put("delG", "delG");
		actorToGSIS.put("delO", "delO");
	}
	/**
	 * This map contains map between workflow action to GMS action.
	 */
	protected static Map<String, String> actorToGSIS = new HashMap<String, String>();

	public static final String[] AVAIL_ACTIONS = {
		"SAdd", "SRemove", "SLeave", "SJoin", "auth",
		"addO", "delO", "delG", "addG"
	};

	protected int objCounter = 0, groupCounter = 0;

	private HashSet<String> objRemoved = new HashSet<String>();
	private HashSet<String> groupRemoved = new HashSet<String>();

	//Actor-->Object created but not added to group.
	private HashMap<String, String> orphanObjects = new HashMap<String, String>();

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

	@Override
	public void action(Action action) {
		super.action(action);
		objRemoved.clear();
		groupRemoved.clear();
		GsisTopState s = (GsisTopState) state;
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
		case "auth": {
			IAccessW<SGPair> lj_k = s.getStrictJoins_K();
			SGPair sgTest = new SGPair(action.params[1], action.params[3]);
			OGPair ogTest = new OGPair(action.params[2], action.params[3]);

			while(lj_k.hasNext()) {
				SGPair sg = lj_k.next();
				//We are looking at a specific (s, o, g) triple. If it's not what we're
				//looking for, skip it and go to the next element.
				if(!sgTest.equals(sg)) continue;

				IAccessW<Integer> lj_v = s.getStrictJoins_V(sg);
				while(lj_v.hasNext()) {
					Integer t1 = lj_v.next();

					IAccessW<OGPair> la_k = s.getStrictAdd_K();
					while(la_k.hasNext()) {
						OGPair og = la_k.next();
						//Not what we're looking for...
						if(!ogTest.equals(og)) continue;

						IAccessW<Integer> la_v = s.getStrictAdd_V(og);
						while(la_v.hasNext()) {
							Integer t2 = la_v.next();
							//SAdd must be done before sjoin for it to take effect.
							//If not, this timestamp is useless, skip!
							if(t2 <= t1) continue;
							boolean testSuccess = true;
							//check: not exist t3: StrictLeave(s, g, t3) => t3 >= t1 OR
							//StrictRemove(s, g, t3) => t3 >= t2
							IAccessW<SGPair> sl_k = s.getStrictLeaves_K();
							while(sl_k.hasNext()) {
								SGPair sg2 = sl_k.next();
								if(!sg2.equals(sgTest)) continue;
								IAccessW<Integer> sl_v = s.getStrictLeaves_V(sg2);
								while(sl_v.hasNext()) {
									Integer t3 = sl_v.next();
									if(t3 >= t1) {
										testSuccess = false;
										break;
									}
								}
								//only one such pair (sg2) = (sg).
								//If this is not it, it would have skipped earlier.
								//We found it and it's got nothing bad, we're done.
								break;
							}
							IAccessW<OGPair> sr_k = s.getStrictRemove_K();
							while(sr_k.hasNext()) {
								OGPair og2 = sr_k.next();
								if(!og2.equals(ogTest)) continue;
								IAccessW<Integer> sr_v = s.getStrictRemove_V(og2);
								while(sr_v.hasNext()) {
									Integer t3 = sr_v.next();
									if(t3 >= t2) {
										testSuccess = false;
										break;
									}
								}
								//only one such pair (og2) = (og).
								//If this is not it, it would have skipped earlier.
								//We found it and it's got nothing bad, we're done.
								break;
							}
							if(testSuccess) {
								return;//auth returns true.
							}
						}
						//No other (o, g) pair exist (otherwise we would skipped earlier)
						//We're done.
						break;
					}
				}
				//No other (s, g) pair exist, we're done.
				break;
			}
		}
		}
	}

	@Override
	public Action addParameters(Action a, Workflow w) {
		String u = a.params[0];
		String obj, group;
		//In this admin-free model, users can join any group they want, but cannot
		//make other users join or leave a group.
		switch(a.name) {
		case "addO":
			objCounter++;
			obj = "m" + (objCounter);
			orphanObjects.put(a.params[0], obj);
			return new Action(a.name, u, obj);
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
			if(orphanObjects.containsKey(a.params[0])) {
				obj = orphanObjects.get(a.params[0]);
				orphanObjects.remove(a.params[0]);
				if(obj == null) return null;
			} else {
				obj = randomObject();
				if(obj == null) return null;
			}
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
		GsisTopState gState = (GsisTopState) state;
		IAccessW<String> it = gState.getG();
		while(it.hasNext()) {
			String group = it.next();
			SGPair test = new SGPair(u, group);
			if(!groupRemoved.contains(group));
			ret.add(group);
			try {
				for(Integer jointime : gState.getStrictJoins_V(test).getList()) {
					//join only counts now if we don't have a leave after
					boolean toDel = true;
					try {
						for(Integer leavetime : gState.getStrictLeaves_V(test).getList()) {
							if(leavetime > jointime) {
								toDel = false;
								break;
							}
						}
					} catch(NullPointerException e) {

					}
					if(toDel) {
						ret.remove(group);
					}
				}
			} catch(NullPointerException e) {

			}
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}

	private String randomGroup() {
		ArrayList<String> ret = new ArrayList<String>();
		GsisTopState gState = (GsisTopState) state;
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
		GsisTopState gState = (GsisTopState) state;
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
		GsisTopState gState = (GsisTopState) state;
		IAccessW<String> it = gState.getS();
		while(it.hasNext()) {
			String obj = it.next();
			ret.add(obj);
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}



}
