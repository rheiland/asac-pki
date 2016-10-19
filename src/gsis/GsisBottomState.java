package gsis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import base.BiAccess;
import base.IAccess;
import base.IAccessW;
import base.InvalidMeasureException;
import base.Property;
import base.WorkloadState;
import base.IAccess.BaseIOType;

public class GsisBottomState extends WorkloadState {

	private static final long serialVersionUID = -862793465056950130L;

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

	@Override
	public WorkloadState createCopy() {
		GsisBottomState ret = new GsisBottomState();
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
		return ret;
	}
	
	public IAccess<GsisBottomState, String> getS(){
		return new IAccess<GsisBottomState, String>(this, S, BaseIOType.Workload);
	}
	
	public IAccess<GsisBottomState, String> getO(){
		return new IAccess<GsisBottomState, String>(this, O, BaseIOType.Workload);
	}
	
	public IAccess<GsisBottomState, String> getG(){
		return new IAccess<GsisBottomState, String>(this, G, BaseIOType.Workload);
	}
	
	BiFunction<SGPair, Integer, Integer> inFuncMap = (sg, st) -> { 
		incrementAccessIO(0, 3);
		return 3;
	}, outFuncMap = (sg, st) -> {
		incrementAccessIO(3, 0);
		return 3;
	};
	
	BiFunction<OGPair, Integer, Integer> inFuncMapO = (og, st) -> { 
		incrementAccessIO(0, 3);
		return 3;
	}, outFuncMapO = (og, st) -> {
		incrementAccessIO(3, 0);
		return 3;
	};
	
	public BiAccess<GsisBottomState, SGPair, Integer> getLiberalJoins(){
		return new BiAccess<GsisBottomState, SGPair, Integer>(this, liberalJoin, inFuncMap, outFuncMap);
	}
	
	public BiAccess<GsisBottomState, SGPair, Integer> getLiberalLeaves(){
		return new BiAccess<GsisBottomState, SGPair, Integer>(this, liberalLeaves, inFuncMap, outFuncMap);
	}
	
	public BiAccess<GsisBottomState, OGPair, Integer> getLiberalAdds(){
		return new BiAccess<GsisBottomState, OGPair, Integer>(this, liberalAdd, inFuncMapO, outFuncMapO);
	}
	
	public BiAccess<GsisBottomState, OGPair, Integer> getLiberalRemoves(){
		return new BiAccess<GsisBottomState, OGPair, Integer>(this, liberalRemove, inFuncMapO, outFuncMapO);
	}

	public IAccessW<String> getSOld() {
		return new IAccessW<String>(S, this, 0);
	}

	public IAccessW<String> getOOld() {
		return new IAccessW<String>(O, this, 0);
	}

	public IAccessW<String> getGOld() {
		return new IAccessW<String>(G, this, 0);
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void occupy(int delta) {
		timestamp += delta;
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
