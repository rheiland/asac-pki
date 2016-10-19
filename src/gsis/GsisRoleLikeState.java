package gsis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import base.IAccessW;
import base.InvalidMeasureException;
import base.Property;
import base.WorkloadState;

public class GsisRoleLikeState extends WorkloadState {

	private Set<String> S = new HashSet<String>();
	private Set<String> O = new HashSet<String>();
	private Set<String> G = new HashSet<String>();

	private int timestamp;
	//(s, g) -> {t}
	private HashMap<SGPair, Set<Integer>> liberalJoins = new HashMap<SGPair, Set<Integer>>();
	private HashMap<SGPair, Set<Integer>> strictLeaves = new HashMap<SGPair, Set<Integer>>();

	//(o, g) -> {t}
	private HashMap<OGPair, Set<Integer>> liberalAdd = new HashMap<OGPair, Set<Integer>>();
	private HashMap<OGPair, Set<Integer>> strictRemove = new HashMap<OGPair, Set<Integer>>();
	/**
	 *
	 */
	private static final long serialVersionUID = 1429876991253466042L;

	@Override
	public WorkloadState createCopy() {
		GsisRoleLikeState ret = new GsisRoleLikeState();
		ret.S.addAll(S);
		ret.O.addAll(O);
		ret.G.addAll(G);
		ret.timestamp = timestamp;
		for(SGPair pair : liberalJoins.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(liberalJoins.get(pair));
			ret.liberalJoins.put(pair, set);
		}
		for(SGPair pair : strictLeaves.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(strictLeaves.get(pair));
			ret.strictLeaves.put(pair, set);
		}
		for(OGPair pair : liberalAdd.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(liberalAdd.get(pair));
			ret.liberalAdd.put(pair, set);
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

	public IAccessW<SGPair> getLiberalJoins_K() {
		return new IAccessW<SGPair>(liberalJoins.keySet(), this, 1);
	}

	public IAccessW<Integer> getLiberalJoins_V(SGPair pair) {
		return new IAccessW<Integer>(liberalJoins.get(pair), this, 2);
	}

	public IAccessW<SGPair> getStrictLeaves_K() {
		return new IAccessW<SGPair>(strictLeaves.keySet(), this, 1);
	}

	public IAccessW<Integer> getStrictLeaves_V(SGPair pair) {
		return new IAccessW<Integer>(strictLeaves.get(pair), this, 2);
	}

	public IAccessW<OGPair> getLiberalAdd_K() {
		return new IAccessW<OGPair>(liberalAdd.keySet(), this, 1);
	}

	public IAccessW<Integer> getLiberalAdd_V(OGPair pair) {
		return new IAccessW<Integer>(liberalAdd.get(pair), this, 2);
	}

	public IAccessW<OGPair> getStrictRemove_K() {
		return new IAccessW<OGPair>(strictRemove.keySet(), this, 1);
	}

	public IAccessW<Integer> getStrictRemove_V(OGPair pair) {
		return new IAccessW<Integer>(strictRemove.get(pair), this, 2);
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
		case "strictLeaves":
		case "liberalAdd":
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
			return liberalJoins;
		case "strictLeaves":
			return strictLeaves;
		case "liberalAdd":
			return liberalAdd;
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
				for(SGPair pair : liberalJoins.keySet()) {
					size += liberalJoins.get(pair).size() * 3;
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
			case "liberalAdd": {
				int size = 0;
				for(OGPair pair : liberalAdd.keySet()) {
					size += liberalAdd.get(pair).size() * 3;
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
				for(SGPair pair : liberalJoins.keySet()) {
					size += liberalJoins.get(pair).size() * 3;
				}
				for(SGPair pair : strictLeaves.keySet()) {
					size += strictLeaves.get(pair).size() * 3;
				}
				for(OGPair pair : liberalAdd.keySet()) {
					size += liberalAdd.get(pair).size() * 3;
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
		ret.add(new Property(this, "strictLeaves", new String[0]));
		ret.add(new Property(this, "liberalAdd", new String[0]));
		ret.add(new Property(this, "strictRemove", new String[0]));
		ret.add(new Property(this, "sizeof", new String[0]));

		ret.addAll(super.getMeasurables());

		return ret;
	}

	public void addLiberalJoin(String subject, String group) {
		SGPair pair = new SGPair(subject, group);
		if(!liberalJoins.containsKey(pair)) {
			liberalJoins.put(pair, new HashSet<Integer>());
		}
		liberalJoins.get(pair).add(timestamp);
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

	public void addLiberalAdd(String object, String group) {
		OGPair pair = new OGPair(object, group);
		if(!liberalAdd.containsKey(pair)) {
			liberalAdd.put(pair, new HashSet<Integer>());
		}
		liberalAdd.get(pair).add(timestamp);
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
