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

public class GsisPsp extends WorkloadScheme {

	/**
	 * This map contains map between workflow action to GMS action.
	 */
	protected static Map<String, String> actorToGSIS = new HashMap<String, String>();

	public GsisPsp(WorkloadState initState) {
		super(initState);
		actorToGSIS.put("SRemove", "SRemove");
		actorToGSIS.put("SLeave", "SLeave");
		actorToGSIS.put("auth", "auth");
		actorToGSIS.put("LAdd", "LAdd");
		actorToGSIS.put("LRemove", "LRemove");
		actorToGSIS.put("LJoin", "LJoin");
		actorToGSIS.put("addG", "addG");
		actorToGSIS.put("addO", "addO");
		actorToGSIS.put("delG", "delG");
		actorToGSIS.put("delO", "delO");
	}

	public void command(Action action) {
		GsisPspState s = (GsisPspState) state;
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
		case "LJoin":
			//subject, group
			s.addLiberalJoin(action.params[1], action.params[2]);
			break;
		case "SRemove":
			s.addStrictRemove(action.params[1], action.params[2]);
			break;
		case "SLeave":
			//subject, group
			s.addStrictLeave(action.params[1], action.params[2]);
			break;
		}
	}

	public void query(Action action) {
		GsisPspState s = (GsisPspState) state;
		switch(action.name) {
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
		case "auth": {
			SGPair sg = new SGPair(action.params[1], action.params[3]);
			OGPair og = new OGPair(action.params[2], action.params[3]);
			List<Integer> joins = s.getLiberalJoins(sg);
			List<Integer> adds = s.getLiberalAdds(og);
			for(Integer t1: joins) {
				for(Integer t2: adds) {
					boolean test1 = true;
					if(t1 >= t2) {
						test1 = false;
					} else {
						List<Integer> sremoves = s.getStrictRemoves(og);
						for(Integer t3: sremoves) {
							if(t3 >= t2) {
								test1 = false;
								break;
							}
						}
						List<Integer> sleaves = s.getStrictLeaves(sg);
						for(Integer t3 : sleaves) {
							if(t3 >= t1 && t2 >= t3) {
								test1 = false;
								break;
							}
						}
					}

					if(test1) {
						lastQueryResult = true;
						return;
					}

					if(t1 > t2) {
						test1 = true;
						List<Integer> removes = s.getRemoveTimes(og);
						for(Integer t3 : removes) {
							if(t3 >= t2 && t1 >= t3) {
								test1 = false;
								break;
							}
						}
						if(test1) {
							lastQueryResult = true;
							break;
						}
					}
				}
			}
			lastQueryResult = false;
			return;
		}
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
		"SRemove", "SLeave", "auth", "LAdd", "LRemove", "LJoin"
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
		case "SRemove":
			group = randomGroup();
			obj = randomObjectInGroup(group);
			if(group == null) return null;
			if(obj == null) return null;
			objRemoved.add(obj);
			return new Action(a.name, u, u, group);
		case "SLeave":
			group = randomGroup(a.params[0]);
			if(group == null) return null;
			return new Action(a.name, u, u, group);
		case "LAdd":
			group = randomGroup();
			if(group == null) return null;
			obj = randomObjectNotIngroup(group);
			if(obj == null) return null;
			return new Action(a.name, u, obj, group);
		case "LRemove":
			group = randomGroup();
			obj = randomObjectInGroup(group);
			if(group == null) return null;
			if(obj == null) return null;
			objRemoved.add(obj);
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

	private String randomGroup(String u) {
		ArrayList<String> ret = new ArrayList<String>();
		GsisPspState gState = (GsisPspState) state;
		for(SGPair sg : gState.getLiberalJoins_K().getList()) {
			String s = sg.subject;
			if(s.equals(u)) {
				String g = sg.group;

				if(gState.isMember(sg)) {
					ret.add(g);
				}
			}
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}

	private String randomObjectInGroup(String g) {
		ArrayList<String> ret = new ArrayList<String>();
		GsisPspState gState = (GsisPspState) state;
		IAccessW<String> it = gState.getO();
		while(it.hasNext()) {
			String obj = it.next();
			if(!objRemoved.contains(obj)) {
				OGPair test = new OGPair(obj, g);
				if(gState.isObjectInGroup(test)) {
					ret.add(obj);
				}
			}
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}

	private String randomObjectNotIngroup(String g) {
		ArrayList<String> ret = new ArrayList<String>();
		GsisPspState gState = (GsisPspState) state;
		IAccessW<String> it = gState.getO();
		while(it.hasNext()) {
			String obj = it.next();
			if(!objRemoved.contains(obj)) {
				OGPair test = new OGPair(obj, g);

				if(!gState.isObjectInGroup(test)) {
					ret.add(obj);
				}
			}
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}

	/**
	 * Return a random group that the user is NOT currently in.
	 * @param u the user to check for
	 * @return
	 */
	private String randomUnoccupiedGroup(String u) {
		ArrayList<String> ret = new ArrayList<String>();
		GsisPspState gState = (GsisPspState) state;
		IAccessW<String> it = gState.getG();
		while(it.hasNext()) {
			String group = it.next();
			SGPair test = new SGPair(u, group);
			if(!groupRemoved.contains(group)) {
				if(!gState.isMember(test)) {
					ret.add(group);
				}
			}
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}


	private String randomGroup() {
		ArrayList<String> ret = new ArrayList<String>();
		GsisPspState gState = (GsisPspState) state;
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
		GsisPspState gState = (GsisPspState) state;
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
		GsisPspState gState = (GsisPspState) state;
		IAccessW<String> it = gState.getS();
		while(it.hasNext()) {
			String obj = it.next();
			ret.add(obj);
		}
		if(ret.size() == 0) return null;
		return ret.get(Simulation.rand.nextInt(ret.size()));
	}
}
