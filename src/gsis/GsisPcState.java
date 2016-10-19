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

public class GsisPcState extends WorkloadState {
	/**
	 *
	 */
	private static final long serialVersionUID = -2232019646390571352L;
	private Set<String> S = new HashSet<String>();
	private Set<String> O = new HashSet<String>();
	private Set<String> G = new HashSet<String>();

	private int timestamp;
	//(s, g) -> {t}
	private HashMap<SGPair, Set<Integer>> liberalJoin = new HashMap<SGPair, Set<Integer>>();
	private HashMap<SGPair, Set<Integer>> liberalLeaves = new HashMap<SGPair, Set<Integer>>();

	//(o, g) -> {t}
	private HashMap<OGPair, Set<Integer>> liberalAdd = new HashMap<OGPair, Set<Integer>>();

	//(s, g) -> {t}
	private HashMap<SGPair, Set<Integer>> strictJoin = new HashMap<SGPair, Set<Integer>>();
	private HashMap<SGPair, Set<Integer>> strictLeaves = new HashMap<SGPair, Set<Integer>>();

	@Override
	public WorkloadState createCopy() {
		GsisPcState ret = new GsisPcState();
		ret.S.addAll(S);
		ret.O.addAll(O);
		ret.G.addAll(G);
		ret.timestamp = timestamp;
		for(SGPair pair : liberalJoin.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(liberalJoin.get(pair));
			ret.liberalJoin.put(pair, set);
		}
		for(SGPair pair : liberalLeaves.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(liberalLeaves.get(pair));
			ret.liberalLeaves.put(pair, set);
		}
		for(OGPair pair : liberalAdd.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(liberalAdd.get(pair));
			ret.liberalAdd.put(pair, set);
		}
		for(SGPair pair : strictJoin.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(strictJoin.get(pair));
			ret.strictJoin.put(pair, set);
		}
		for(SGPair pair : strictLeaves.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(strictLeaves.get(pair));
			ret.strictLeaves.put(pair, set);
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

	public IAccessW<SGPair> getStrictJoins_K() {
		return new IAccessW<SGPair>(strictJoin.keySet(), this, 1);
	}

	public IAccessW<Integer> getStrictJoins_V(SGPair pair) {
		return new IAccessW<Integer>(strictJoin.get(pair), this, 2);
	}

	public IAccessW<SGPair> getStrictLeaves_K() {
		return new IAccessW<SGPair>(strictLeaves.keySet(), this, 1);
	}

	public IAccessW<Integer> getStrictLeaves_V(SGPair pair) {
		return new IAccessW<Integer>(strictLeaves.get(pair), this, 2);
	}

	public IAccessW<SGPair> getLiberalJoins_K() {
		return new IAccessW<SGPair>(liberalJoin.keySet(), this, 1);
	}

	public IAccessW<Integer> getLiberalJoins_V(SGPair pair) {
		return new IAccessW<Integer>(liberalJoin.get(pair), this, 2);
	}

	public IAccessW<SGPair> getLiberalLeaves_K() {
		return new IAccessW<SGPair>(liberalLeaves.keySet(), this, 1);
	}

	public IAccessW<Integer> getLiberalLeaves_V(SGPair pair) {
		return new IAccessW<Integer>(liberalLeaves.get(pair), this, 2);
	}

	public IAccessW<OGPair> getLiberalAdd_K() {
		return new IAccessW<OGPair>(liberalAdd.keySet(), this, 1);
	}

	public IAccessW<Integer> getLiberalAdd_V(OGPair pair) {
		return new IAccessW<Integer>(liberalAdd.get(pair), this, 2);
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
		case "strictJoins":
		case "strictLeaves":
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
		case "liberalLeaves":
			return liberalLeaves;
		case "liberalAdd":
			return liberalAdd;
		case "strictJoins":
			return strictJoin;
		case "strictLeaves":
			return strictLeaves;
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
			case "liberalLeaves": {
				int size = 0;
				for(SGPair pair : liberalLeaves.keySet()) {
					size += liberalLeaves.get(pair).size() * 3;
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
			case "strictJoins": {
				int size = 0;
				for(SGPair pair : strictJoin.keySet()) {
					size += strictJoin.get(pair).size() * 3;
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
			case "ALL": {
				int size = 0;
				size += S.size() + O.size() + G.size() + 1;
				for(SGPair pair : liberalJoin.keySet()) {
					size += liberalJoin.get(pair).size() * 3;
				}
				for(SGPair pair : liberalLeaves.keySet()) {
					size += liberalLeaves.get(pair).size() * 3;
				}
				for(OGPair pair : liberalAdd.keySet()) {
					size += liberalAdd.get(pair).size() * 3;
				}
				for(SGPair pair : strictJoin.keySet()) {
					size += strictJoin.get(pair).size() * 3;
				}
				for(SGPair pair : strictLeaves.keySet()) {
					size += strictLeaves.get(pair).size() * 3;
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
		ret.add(new Property(this, "strictJoins", new String[0]));
		ret.add(new Property(this, "strictLeaves", new String[0]));
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

	public void addLiberalLeave(String subject, String group) {
		SGPair pair = new SGPair(subject, group);
		if(!liberalLeaves.containsKey(pair)) {
			liberalLeaves.put(pair, new HashSet<Integer>());
		}
		liberalLeaves.get(pair).add(timestamp);
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

	public void addStrictJoin(String subject, String group) {
		SGPair pair = new SGPair(subject, group);
		if(!strictJoin.containsKey(pair)) {
			strictJoin.put(pair, new HashSet<Integer>());
		}
		strictJoin.get(pair).add(timestamp);
		incrementAccessIO(0, 2);
		occupy(1);
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
	public List<Integer> getJoinTimes(SGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		try {
			IAccessW<Integer> s = getStrictJoins_V(pair);
			while(s.hasNext()) {
				ret.add(s.next());
			}
		} catch(NullPointerException e) {

		}
		try {
			IAccessW<Integer> l = getLiberalJoins_V(pair);
			while(l.hasNext()) {
				ret.add(l.next());
			}
		} catch(NullPointerException e) {

		}
		return ret;
	}

	public List<Integer> getLeaveTimes(SGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		try {
			IAccessW<Integer> s = getStrictLeaves_V(pair);

			while(s.hasNext()) {
				ret.add(s.next());
			}
		} catch(NullPointerException e) {

		}
		try {
			IAccessW<Integer> l = getLiberalLeaves_V(pair);
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

	public List<Integer> getLiberalLeaves(SGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		try {
			IAccessW<Integer> l = getLiberalLeaves_V(pair);
			while(l.hasNext()) {
				ret.add(l.next());
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

	public boolean isObjectInGroup(OGPair pair) {
		if(liberalAdd.containsKey(pair)) {
			incrementAccessIO(3, 0);
			return true;
		}
		incrementAccessIO(3, 0);
		return false;
	}

}
