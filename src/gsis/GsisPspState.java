package gsis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import base.IAccessW;
import base.InvalidMeasureException;
import base.Property;
import base.WorkloadState;

public class GsisPspState extends WorkloadState {

	private static final long serialVersionUID = 4209472320580684817L;

	private Set<String> S = new HashSet<String>();
	private Set<String> O = new HashSet<String>();
	private Set<String> G = new HashSet<String>();

	private int timestamp;
	//(s, g) -> {t}
	private HashMap<SGPair, Set<Integer>> liberalJoin = new HashMap<SGPair, Set<Integer>>();

	//(o, g) -> {t}
	private HashMap<OGPair, Set<Integer>> liberalAdd = new HashMap<OGPair, Set<Integer>>();
	private HashMap<OGPair, Set<Integer>> liberalRemove = new HashMap<OGPair, Set<Integer>>();


	private HashMap<SGPair, Set<Integer>> strictLeaves = new HashMap<SGPair, Set<Integer>>();

	private HashMap<OGPair, Set<Integer>> strictRemove = new HashMap<OGPair, Set<Integer>>();

	public boolean containsLJoinKey(SGPair pair) {
		return liberalJoin.containsKey(pair);
	}

	public boolean containsLAddKey(OGPair pair) {
		return liberalAdd.containsKey(pair);
	}

	public boolean containsLRemoveKey(OGPair pair) {
		return liberalRemove.containsKey(pair);
	}

	public boolean containsSLeave(SGPair pair) {
		return strictLeaves.containsKey(pair);
	}

	public boolean containsSRemove(OGPair pair) {
		return strictRemove.containsKey(pair);
	}

	public boolean isMember(SGPair pair) {
		if(liberalJoin.containsKey(pair)) {
			ArrayList<Integer> joins = new ArrayList<Integer>(liberalJoin.get(pair));
			int jointime = Collections.max(joins);
			if(strictLeaves.containsKey(pair)) {
				ArrayList<Integer> sortedLeaves = new ArrayList<Integer>(strictLeaves.get(pair));

				if(jointime > Collections.max(sortedLeaves)) {
					incrementAccessIO(2, 0);
					return true;
				}
				incrementAccessIO(2, 0);
				return false;
			}
			incrementAccessIO(2, 0);
			return true;
		}
		incrementAccessIO(2, 0);
		return false;
	}

	public boolean isObjectInGroup(OGPair pair) {
		if(liberalAdd.containsKey(pair)) {
			ArrayList<Integer> adds = new ArrayList<Integer>(liberalAdd.get(pair));
			int addTime = Collections.max(adds);
			int removetime = -1;

			if(liberalRemove.containsKey(pair)) {
				ArrayList<Integer> removes = new ArrayList<Integer>(liberalRemove.get(pair));
				removetime = Collections.max(removes);
			}

			if(strictRemove.containsKey(pair)) {
				ArrayList<Integer> removes = new ArrayList<Integer>(strictRemove.get(pair));
				removetime = Math.max(removetime, Collections.max(removes));
			}

			if(addTime > removetime) {
				incrementAccessIO(3, 0);
				return true;
			}
		}
		incrementAccessIO(3, 0);
		return false;
	}

	@Override
	public WorkloadState createCopy() {
		GsisPspState ret = new GsisPspState();
		ret.S.addAll(S);
		ret.O.addAll(O);
		ret.G.addAll(G);
		ret.timestamp = timestamp;
		for(SGPair pair : liberalJoin.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(liberalJoin.get(pair));
			ret.liberalJoin.put(pair, set);
		}
		for(OGPair pair : liberalAdd.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(liberalAdd.get(pair));
			ret.liberalAdd.put(pair, set);
		}
		for(OGPair pair : liberalRemove.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(liberalRemove.get(pair));
			ret.liberalRemove.put(pair, set);
		}
		for(SGPair pair : strictLeaves.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(strictLeaves.get(pair));
			ret.strictLeaves.put(pair, set);
		}
		for(OGPair pair : strictRemove.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(strictRemove.get(pair));
			ret.strictRemove.put(pair, set);
		}
		return ret;
	}

	public IAccessW<String> getS() {
		return new IAccessW<String>(S, this, 0);
	}

	public IAccessW<String> getO() {
		return new IAccessW<String>(O, this, 0);
	}

	public IAccessW<String> getG() {
		return new IAccessW<String>(G, this, 0);
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void occupy(int delta) {
		timestamp += delta;
	}


	public IAccessW<SGPair> getStrictLeaves_K() {
		return new IAccessW<SGPair>(strictLeaves.keySet(), this, 1);
	}

	public IAccessW<Integer> getStrictLeaves_V(SGPair pair) {
		return new IAccessW<Integer>(strictLeaves.get(pair), this, 2);
	}

	public IAccessW<OGPair> getStrictRemove_K() {
		return new IAccessW<OGPair>(strictRemove.keySet(), this, 1);
	}

	public IAccessW<Integer> getStrictRemove_V(OGPair pair) {
		return new IAccessW<Integer>(strictRemove.get(pair), this, 2);
	}

	public IAccessW<SGPair> getLiberalJoins_K() {
		return new IAccessW<SGPair>(liberalJoin.keySet(), this, 1);
	}

	public IAccessW<Integer> getLiberalJoins_V(SGPair pair) {
		return new IAccessW<Integer>(liberalJoin.get(pair), this, 2);
	}


	public IAccessW<OGPair> getLiberalAdd_K() {
		return new IAccessW<OGPair>(liberalAdd.keySet(), this, 1);
	}

	public IAccessW<Integer> getLiberalAdd_V(OGPair pair) {
		return new IAccessW<Integer>(liberalAdd.get(pair), this, 2);
	}

	public IAccessW<OGPair> getLiberalRemove_K() {
		return new IAccessW<OGPair>(liberalRemove.keySet(), this, 1);
	}

	public IAccessW<Integer> getLiberalRemove_V(OGPair pair) {
		return new IAccessW<Integer>(liberalRemove.get(pair), this, 2);
	}

	@Override
	public boolean isMeasurable(Property p) {
		Property qTest = new Property(this, "__test", new String[0]);
		if(!qTest.matchQualifier(p)) {
			return false;
		}
		switch(p.name) {
		case "S":
		case "O":
		case "G":
		case "timestamp":
		case "liberalJoins":
		case "liberalLeaves":
		case "liberalAdd":
		case "liberalRemove":
		case "strictJoins":
		case "strictLeaves":
		case "strictAdd":
		case "strictRemove":
		case "sizeof":
			return true;
		}
		return super.isMeasurable(p);
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
		case "S":
			return S;
		case "O":
			return O;
		case "G":
			return G;
		case "timestamp":
			return timestamp;
		case "liberalJoins":
			return liberalJoin;
		case "liberalAdd":
			return liberalAdd;
		case "liberalRemove":
			return liberalRemove;
		case "strictLeaves":
			return strictLeaves;
		case "strictRemove":
			return strictRemove;
		case "sizeof":
			switch(p.params.get(0)) {
			case "S":
				return S.size();
			case "O":
				return O.size();
			case "G":
				return G.size();
			case "timestamp":
				return 1;
			case "liberalJoins": {
				int size = 0;
				for(SGPair pair : liberalJoin.keySet()) {
					size += liberalJoin.get(pair).size() * 3;
				}
				return size;
			}
			case "liberalAdd": {
				int size = 0;
				for(OGPair pair : liberalAdd.keySet()) {
					size += liberalAdd.get(pair).size() * 3;
				}
				return size;
			}
			case "liberalRemove": {
				int size = 0;
				for(OGPair pair : liberalRemove.keySet()) {
					size += liberalRemove.get(pair).size() * 3;
				}
				return size;
			}
			case "strictLeaves": {
				int size = 0;
				for(SGPair pair : strictLeaves.keySet()) {
					size += strictLeaves.get(pair).size() * 3;
				}
				return size;
			}
			case "strictRemove": {
				int size = 0;
				for(OGPair pair : strictRemove.keySet()) {
					size += strictRemove.get(pair).size() * 3;
				}
				return size;
			}
			case "ALL": {
				int size = 0;
				size += S.size() + O.size() + G.size() + 1;
				for(SGPair pair : liberalJoin.keySet()) {
					size += liberalJoin.get(pair).size() * 3;
				}
				for(OGPair pair : liberalAdd.keySet()) {
					size += liberalAdd.get(pair).size() * 3;
				}
				for(OGPair pair : liberalRemove.keySet()) {
					size += liberalRemove.get(pair).size() * 3;
				}
				for(SGPair pair : strictLeaves.keySet()) {
					size += strictLeaves.get(pair).size() * 3;
				}
				for(OGPair pair : strictRemove.keySet()) {
					size += strictRemove.get(pair).size() * 3;
				}
				return size;
			}

			}
		}
		throw new InvalidMeasureException(p);
	}

	@Override
	public Set<Property> getMeasurables() {
		HashSet<Property> ret = new HashSet<Property>();
		ret.add(new Property(this, "S", new String[0]));
		ret.add(new Property(this, "O", new String[0]));
		ret.add(new Property(this, "G", new String[0]));
		ret.add(new Property(this, "timestamp", new String[0]));
		ret.add(new Property(this, "liberalJoins", new String[0]));
		ret.add(new Property(this, "liberalLeaves", new String[0]));
		ret.add(new Property(this, "liberalAdd", new String[0]));
		ret.add(new Property(this, "liberalRemove", new String[0]));
		ret.add(new Property(this, "strictJoins", new String[0]));
		ret.add(new Property(this, "strictLeaves", new String[0]));
		ret.add(new Property(this, "strictAdd", new String[0]));
		ret.add(new Property(this, "strictRemove", new String[0]));
		ret.add(new Property(this, "sizeof", new String[0]));

		ret.addAll(super.getMeasurables());

		return ret;
	}

	public void addLiberalJoin(String subject, String group) {
		SGPair pair = new SGPair(subject, group);
		if(!liberalJoin.containsKey(pair)) {
			liberalJoin.put(pair, new HashSet<Integer>());
		}
		liberalJoin.get(pair).add(timestamp);
		incrementAccessIO(0, 2);
		occupy(1);
	}

	public void addLiberalAdd(String object, String group) {
		OGPair pair = new OGPair(object, group);
		if(!liberalAdd.containsKey(pair)) {
			liberalAdd.put(pair, new HashSet<Integer>());
		}
		liberalAdd.get(pair).add(timestamp);
		incrementAccessIO(0, 2);
		occupy(1);
	}

	public void addLiberalRemove(String object, String group) {
		OGPair pair = new OGPair(object, group);
		if(!liberalRemove.containsKey(pair)) {
			liberalRemove.put(pair, new HashSet<Integer>());
		}
		liberalRemove.get(pair).add(timestamp);
		incrementAccessIO(0, 2);
		occupy(1);
	}

	public void addGroup(String group) {
		if(!G.contains(group)) {
			G.add(group);
			incrementAccessIO(0, 1);
		}
	}

	public void addObject(String object) {
		if(!O.contains(object)) {
			O.add(object);
			incrementAccessIO(0, 1);
		}
	}

	public void addStrictLeave(String subject, String group) {
		SGPair pair = new SGPair(subject, group);
		if(!strictLeaves.containsKey(pair)) {
			strictLeaves.put(pair, new HashSet<Integer>());
		}
		strictLeaves.get(pair).add(timestamp);
		incrementAccessIO(0, 2);
		occupy(1);
	}

	public void addStrictRemove(String object, String group) {
		OGPair pair = new OGPair(object, group);
		if(!strictRemove.containsKey(pair)) {
			strictRemove.put(pair, new HashSet<Integer>());
		}
		strictRemove.get(pair).add(timestamp);
		incrementAccessIO(0, 2);
		occupy(1);
	}

	public List<Integer> getRemoveTimes(OGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		try {
			IAccessW<Integer> s = getStrictRemove_V(pair);
			while(s.hasNext()) {
				ret.add(s.next());
			}
		} catch(NullPointerException e) {

		}

		try {
			IAccessW<Integer> l = getLiberalRemove_V(pair);
			while(l.hasNext()) {
				ret.add(l.next());
			}
		} catch(NullPointerException e) {

		}
		return ret;
	}

	public List<Integer> getStrictLeaves(SGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		try {
			IAccessW<Integer> s = getStrictLeaves_V(pair);
			while(s.hasNext()) {
				ret.add(s.next());
			}
		} catch(NullPointerException e) {

		}
		return ret;
	}

	public List<Integer> getLiberalAdds(OGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		try {
			IAccessW<Integer> l = getLiberalAdd_V(pair);
			while(l.hasNext()) {
				ret.add(l.next());
			}
		} catch(NullPointerException e) {

		}
		return ret;
	}

	public List<Integer> getLiberalJoins(SGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		try {
			IAccessW<Integer> l = getLiberalJoins_V(pair);
			while(l.hasNext()) {
				ret.add(l.next());
			}
		} catch(NullPointerException e) {

		}
		return ret;
	}

	public List<Integer> getStrictRemoves(OGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		try {
			IAccessW<Integer> s = getStrictRemove_V(pair);
			while(s.hasNext()) {
				ret.add(s.next());
			}
		} catch(NullPointerException e) {

		}
		return ret;
	}

	public void delObject(String object) {
		if(O.contains(object)) {
			O.remove(object);
			incrementAccessIO(0, 1);
		}
	}

	public void delGroup(String group) {
		if(G.contains(group)) {
			G.remove(group);
			incrementAccessIO(0, 1);
		}
	}

	/**
	 * Should only be used in init stage!
	 * @param subject
	 */
	public void addSubject(String subject) {
		if(!S.contains(subject)) {
			S.add(subject);
			incrementAccessIO(0, 1);
		}
	}

}
