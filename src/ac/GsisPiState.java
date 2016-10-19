package ac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import base.State;
import base.IAccessAC;
import base.InvalidMeasureException;
import base.Property;

public class GsisPiState extends State {
	private Set<String> S = new HashSet<String>();
	private Set<String> O = new HashSet<String>();
	private Set<String> G = new HashSet<String>();

	private int timestamp;
	//(s, g) -> {t}
	private HashMap<SGPair, Set<Integer>> liberalJoin = new HashMap<SGPair, Set<Integer>>();
	private HashMap<SGPair, Set<Integer>> liberalLeaves = new HashMap<SGPair, Set<Integer>>();

	//(o, g) -> {t}
	private HashMap<OGPair, Set<Integer>> liberalAdd = new HashMap<OGPair, Set<Integer>>();
	private HashMap<OGPair, Set<Integer>> liberalRemove = new HashMap<OGPair, Set<Integer>>();

	//(s, g) -> {t}
	private HashMap<SGPair, Set<Integer>> strictJoin = new HashMap<SGPair, Set<Integer>>();
	private HashMap<SGPair, Set<Integer>> strictLeaves = new HashMap<SGPair, Set<Integer>>();

	//(o, g) -> {t}
	private HashMap<OGPair, Set<Integer>> strictAdd = new HashMap<OGPair, Set<Integer>>();
	private HashMap<OGPair, Set<Integer>> strictRemove = new HashMap<OGPair, Set<Integer>>();

	@Override
	public State createCopy() {
		GsisPiState ret = new GsisPiState();
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
		for(OGPair pair : liberalRemove.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(liberalRemove.get(pair));
			ret.liberalRemove.put(pair, set);
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
		for(OGPair pair : strictAdd.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(strictAdd.get(pair));
			ret.strictAdd.put(pair, set);
		}
		for(OGPair pair : strictRemove.keySet()) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(strictRemove.get(pair));
			ret.strictRemove.put(pair, set);
		}
		return ret;
	}

	public IAccessAC<String> getS() {
		return new IAccessAC<String>(S, this, 1);
	}

	public IAccessAC<String> getO() {
		return new IAccessAC<String>(O, this, 1);
	}

	public IAccessAC<String> getG() {
		return new IAccessAC<String>(G, this, 1);
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimeStamp(int time) {
		timestamp = time;
	}

	public void occupy(int delta) {
		timestamp += delta;
	}

	public IAccessAC<SGPair> getStrictJoins_K() {
		return new IAccessAC<SGPair>(strictJoin.keySet(), this, 1);
	}

	public IAccessAC<Integer> getStrictJoins_V(SGPair pair) {
		return new IAccessAC<Integer>(strictJoin.get(pair), this, 2);
	}

	public IAccessAC<SGPair> getStrictLeaves_K() {
		return new IAccessAC<SGPair>(strictLeaves.keySet(), this, 1);
	}

	public IAccessAC<Integer> getStrictLeaves_V(SGPair pair) {
		return new IAccessAC<Integer>(strictLeaves.get(pair), this, 2);
	}

	public IAccessAC<OGPair> getStrictAdd_K() {
		return new IAccessAC<OGPair>(strictAdd.keySet(), this, 1);
	}

	public IAccessAC<Integer> getStrictAdd_V(OGPair pair) {
		return new IAccessAC<Integer>(strictAdd.get(pair), this, 2);
	}

	public IAccessAC<OGPair> getStrictRemove_K() {
		return new IAccessAC<OGPair>(strictRemove.keySet(), this, 1);
	}

	public IAccessAC<Integer> getStrictRemove_V(OGPair pair) {
		return new IAccessAC<Integer>(strictRemove.get(pair), this, 2);
	}

	public IAccessAC<SGPair> getLiberalJoins_K() {
		return new IAccessAC<SGPair>(liberalJoin.keySet(), this, 1);
	}

	public IAccessAC<Integer> getLiberalJoins_V(SGPair pair) {
		return new IAccessAC<Integer>(liberalJoin.get(pair), this, 2);
	}

	public IAccessAC<SGPair> getLiberalLeaves_K() {
		return new IAccessAC<SGPair>(liberalLeaves.keySet(), this, 1);
	}

	public IAccessAC<Integer> getLiberalLeaves_V(SGPair pair) {
		return new IAccessAC<Integer>(liberalLeaves.get(pair), this, 2);
	}

	public IAccessAC<OGPair> getLiberalAdd_K() {
		return new IAccessAC<OGPair>(liberalAdd.keySet(), this, 1);
	}

	public IAccessAC<Integer> getLiberalAdd_V(OGPair pair) {
		return new IAccessAC<Integer>(liberalAdd.get(pair), this, 2);
	}

	public IAccessAC<OGPair> getLiberalRemove_K() {
		return new IAccessAC<OGPair>(liberalRemove.keySet(), this, 1);
	}

	public IAccessAC<Integer> getLiberalRemove_V(OGPair pair) {
		return new IAccessAC<Integer>(liberalRemove.get(pair), this, 2);
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
		case "liberalLeaves":
			return liberalLeaves;
		case "liberalAdd":
			return liberalAdd;
		case "liberalRemove":
			return liberalRemove;
		case "strictJoins":
			return strictJoin;
		case "strictLeaves":
			return strictLeaves;
		case "strictAdd":
			return strictAdd;
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
			case "liberalRemove": {
				int size = 0;
				for(OGPair pair : liberalRemove.keySet()) {
					size += liberalRemove.get(pair).size() * 3;
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
			case "strictAdd": {
				int size = 0;
				for(OGPair pair : strictAdd.keySet()) {
					size += strictAdd.get(pair).size() * 3;
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
				for(SGPair pair : liberalLeaves.keySet()) {
					size += liberalLeaves.get(pair).size() * 3;
				}
				for(OGPair pair : liberalAdd.keySet()) {
					size += liberalAdd.get(pair).size() * 3;
				}
				for(OGPair pair : liberalRemove.keySet()) {
					size += liberalRemove.get(pair).size() * 3;
				}
				for(SGPair pair : strictJoin.keySet()) {
					size += strictJoin.get(pair).size() * 3;
				}
				for(SGPair pair : strictLeaves.keySet()) {
					size += strictLeaves.get(pair).size() * 3;
				}
				for(OGPair pair : strictAdd.keySet()) {
					size += strictAdd.get(pair).size() * 3;
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

	public void addSubject(String subject) {
		if(!S.contains(subject)) {
			S.add(subject);
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

	public void addStrictAdd(String object, String group) {
		OGPair pair = new OGPair(object, group);
		if(!strictAdd.containsKey(pair)) {
			strictAdd.put(pair, new HashSet<Integer>());
		}
		strictAdd.get(pair).add(timestamp);
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

	public List<Integer> getJoinTimes(SGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		IAccessAC<Integer> s = getStrictJoins_V(pair);
		IAccessAC<Integer> l = getLiberalJoins_V(pair);
		while(s.hasNext()) {
			ret.add(s.next());
		}
		while(l.hasNext()) {
			ret.add(l.next());
		}
		return ret;
	}

	public List<Integer> getLeaveTimes(SGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		IAccessAC<Integer> s = getStrictLeaves_V(pair);
		IAccessAC<Integer> l = getLiberalLeaves_V(pair);
		while(s.hasNext()) {
			ret.add(s.next());
		}
		while(l.hasNext()) {
			ret.add(l.next());
		}
		return ret;
	}

	public List<Integer> getAddTimes(OGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		IAccessAC<Integer> s = getStrictAdd_V(pair);
		IAccessAC<Integer> l = getLiberalAdd_V(pair);
		while(s.hasNext()) {
			ret.add(s.next());
		}
		while(l.hasNext()) {
			ret.add(l.next());
		}
		return ret;
	}

	public List<Integer> getRemoveTimes(OGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		IAccessAC<Integer> s = getStrictRemove_V(pair);
		IAccessAC<Integer> l = getLiberalRemove_V(pair);
		while(s.hasNext()) {
			ret.add(s.next());
		}
		while(l.hasNext()) {
			ret.add(l.next());
		}
		return ret;
	}

	public List<Integer> getStrictLeaves(SGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		IAccessAC<Integer> s = getStrictLeaves_V(pair);
		while(s.hasNext()) {
			ret.add(s.next());
		}
		return ret;
	}

	public List<Integer> getLiberalLeaves(SGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		IAccessAC<Integer> l = getLiberalLeaves_V(pair);
		while(l.hasNext()) {
			ret.add(l.next());
		}
		return ret;
	}

	public List<Integer> getLiberalAdds(OGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		IAccessAC<Integer> l = getLiberalAdd_V(pair);
		while(l.hasNext()) {
			ret.add(l.next());
		}
		return ret;
	}

	public List<Integer> getLiberalJoins(SGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		IAccessAC<Integer> l = getLiberalJoins_V(pair);
		while(l.hasNext()) {
			ret.add(l.next());
		}
		return ret;
	}

	public List<Integer> getStrictRemoves(OGPair pair) {
		List<Integer> ret = new ArrayList<Integer>();
		IAccessAC<Integer> s = getStrictRemove_V(pair);
		while(s.hasNext()) {
			ret.add(s.next());
		}
		return ret;
	}


}
