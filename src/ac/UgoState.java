package ac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import base.IAccessACH;
import base.Implementation;
import base.State;
import base.IAccessAC;
import base.InvalidMeasureException;
import base.Property;
import static base.Implementation.numSubs;

public class UgoState extends State {

	private class ORPair {
		public String obj;
		public String right;

		public ORPair(String obj, String right) {
			this.obj = obj;
			this.right = right;
		}

		@Override
		public String toString() {
			return "(" + obj + "," + right + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
			result = prime * result + ((right == null) ? 0 : right.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			ORPair other = (ORPair) obj;
			if(!getOuterType().equals(other.getOuterType()))
				return false;
			if(this.obj == null) {
				if(other.obj != null)
					return false;
			} else if(!this.obj.equals(other.obj))
				return false;
			if(right == null) {
				if(other.right != null)
					return false;
			} else if(!right.equals(other.right))
				return false;
			return true;
		}
		private UgoState getOuterType() {
			return UgoState.this;
		}


	}

	HashSet<String> S = new HashSet<String>(), O = new HashSet<String>(), G = new HashSet<String>(),
	R = new HashSet<String>();

	//HashSet<SGPair> member = new HashSet<SGPair>();
	//S->G
	HashMap<String, HashSet<String>> member = new HashMap<String, HashSet<String>>();
	HashMap<String, String> owner = new HashMap<String, String>();//O->S
	HashMap<String, String> group = new HashMap<String, String>();//O->G

	HashSet<ORPair> ownerRight = new HashSet<ORPair>();
	HashSet<ORPair> groupRight = new HashSet<ORPair>();
	HashSet<ORPair> otherRight = new HashSet<ORPair>();

	public IAccessAC<String> getS() {
		return new IAccessACH(S, this, 0);
	}
	public IAccessAC<String> getO() {
		return new IAccessACH(O, this, 0);
	}
	public IAccessAC<String> getG() {
		return new IAccessACH(G, this, 0);
	}
	public IAccessAC<String> getR() {
		return new IAccessACH(R, this, 0);
	}
	public IAccessAC<String> getMember_K() {
		return new IAccessACH(member.keySet(), this, 0);
	}
	public IAccessAC<String> getMember_V(String s){
		return new IAccessACH(member.get(s), this, numSubs(s));
	}
	
	public IAccessAC<String> getOwner_K() {
		return new IAccessACH(owner.keySet(), this, 0);
	}

	public String getOwner_V(String o) {
		String ret = owner.get(o);
		incrementAccessIO(numSubs(o) + numSubs(ret), 0);
		return ret;
	}

	public IAccessAC<String> getGroup_K() {
		return new IAccessACH(group.keySet(), this, 0);
	}

	public String getGroup_V(String o) {
		String ret = group.get(o);
		incrementAccessIO(numSubs(o) + numSubs(ret), 0);
		return ret;
	}

	public IAccessAC<ORPair> getOwnerRight() {
		return new IAccessAC<ORPair>(ownerRight, this, 1);
	}

	public IAccessAC<ORPair> getGroupRight() {
		return new IAccessAC<ORPair>(groupRight, this, 1);
	}

	public IAccessAC<ORPair> getOtherRight() {
		return new IAccessAC<ORPair>(otherRight, this, 1);
	}

	public void addMember(String s, String group) {
		int sIO = numSubs(s);
		int gIO = numSubs(group);
		if (! member.containsKey(s)){
			member.put(s, new HashSet<String>());
		}
		member.get(s).add(group);
		incrementAccessIO(0, sIO + gIO);
	}

	public void delMember(String s, String group) {
		int sIO = numSubs(s);
		int gIO = numSubs(group);
		if (member.containsKey(s)){
			if (member.get(s).contains(group)){
				incrementAccessIO(0, gIO + sIO);
				member.get(s).remove(group);
			}
		}
	}

	public void addS(String s) {
		if(!S.contains(s)) {
			S.add(s);
			incrementAccessIO(0, numSubs(s));
		}
	}

	public void delS(String s) {
		if(S.contains(s)) {
			S.remove(s);
			incrementAccessIO(0, numSubs(s));
		}
	}

	public void addO(String o) {
		if(!O.contains(o)) {
			O.add(o);
			incrementAccessIO(0, numSubs(o));
		}
	}

	public void delO(String o) {
		if(O.contains(o)) {
			O.remove(o);
			incrementAccessIO(0, numSubs(o));
		}
		if(owner.containsKey(o)) {
			int io = numSubs(o) + numSubs(owner.get(o)); 
			owner.remove(o);
			incrementAccessIO(0, io);
		}
		if(group.containsKey(o)) {
			int io = numSubs(o) + numSubs(group.get(o));
			group.remove(o);
			incrementAccessIO(0, io);
		}
		ArrayList<ORPair> ors = new ArrayList<ORPair>();
		for(ORPair or : ownerRight) {
			if(or.obj.equals(o)) {
				ors.add(or);
			}
		}
		for(ORPair or : ors) {
			ownerRight.remove(or);
			incrementAccessIO(0, 2);
		}
		ors.clear();

		for(ORPair or : groupRight) {
			if(or.obj.equals(o)) {
				ors.add(or);
			}
		}
		for(ORPair or : ors) {
			groupRight.remove(or);
			incrementAccessIO(0, 2);
		}
		ors.clear();

		for(ORPair or : otherRight) {
			if(or.obj.equals(o)) {
				ors.add(or);
			}
		}
		for(ORPair or : ors) {
			otherRight.remove(or);
			incrementAccessIO(0, 2);
		}
		ors.clear();
	}

	public void addG(String g) {
		if(!G.contains(g)) {
			G.add(g);
			incrementAccessIO(0, numSubs(g));
		}
	}

	public void delG(String g) {
		if(G.contains(g)) {
			G.remove(g);
			incrementAccessIO(0, numSubs(g));
		}
		for (String s : member.keySet()){
			delMember(s, g);
		}

		ArrayList<String> groupDel = new ArrayList<String>();
		for(String o : group.keySet()) {
			if(group.get(o).equals(g)) {
				groupDel.add(o);
			}
		}
		for(String o : groupDel) {
			group.remove(o);
			incrementAccessIO(0, 1 + numSubs(o));
		}

	}

	public void changeOwner(String obj, String subj) {
		owner.put(obj, subj);
		incrementAccessIO(0, numSubs(obj) + numSubs(subj));
	}

	public void changeGroup(String obj, String group) {
		this.group.put(obj, group);
		incrementAccessIO(0, numSubs(obj) + numSubs(group));
	}

	public void grantOther(String o, String r) {
		ORPair or = new ORPair(o, r);
		if(!otherRight.contains(or)) {
			otherRight.add(or);
			incrementAccessIO(0, 2);
		}
	}

	public void revokeOther(String o, String r) {
		ORPair or = new ORPair(o, r);
		if(otherRight.contains(or)) {
			otherRight.remove(or);
			incrementAccessIO(0, 2);
		}
	}

	public void grantGroup(String o, String r) {
		ORPair or = new ORPair(o, r);
		if(!groupRight.contains(or)) {
			groupRight.add(or);
			incrementAccessIO(0, 2);
		}
	}

	public void revokeGroup(String o, String r) {
		ORPair or = new ORPair(o, r);
		if(groupRight.contains(or)) {
			groupRight.remove(or);
			incrementAccessIO(0, 2);
		}
	}

	public void grantOwner(String o, String r) {
		ORPair or = new ORPair(o, r);
		if(!ownerRight.contains(or)) {
			ownerRight.add(or);
			incrementAccessIO(0, 2);
		}
	}

	public void revokeOwner(String o, String r) {
		ORPair or = new ORPair(o, r);
		if(ownerRight.contains(or)) {
			ownerRight.remove(or);
			incrementAccessIO(0, 2);
		}
	}

	public UgoState() {
		R.add("read");
		R.add("write");
		R.add("execute");
	}

	@Override
	public State createCopy() {
		UgoState ret = new UgoState();
		ret.S.addAll(S);
		ret.O.addAll(O);
		ret.G.addAll(G);
		ret.R.addAll(R);
		ret.member = (HashMap<String, HashSet<String>> )member.clone();
		for(String o : owner.keySet()) {
			ret.owner.put(o, owner.get(o));
		}
		for(String o: group.keySet()) {
			ret.group.put(o, group.get(o));
		}
		ret.ownerRight.addAll(ownerRight);
		ret.groupRight.addAll(groupRight);
		ret.otherRight.addAll(otherRight);
		return ret;
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
		case "R":
		case "member":
		case "owner":
		case "group":
		case "ownerRight":
		case "groupRight":
		case "otherRight":
			return true;
		case "sizeof":
			switch(p.params.get(0)) {
			case "S":
			case "O":
			case "G":
			case "R":
			case "member":
			case "owner":
			case "group":
			case "ownerRight":
			case "groupRight":
			case "otherRight":
			case "ALL":
				return true;
			}
		}
		return super.isMeasurable(p);
	}

	@Override
	public Set<Property> getMeasurables() {
		HashSet<Property> ret = new HashSet<Property>();
		ret.add(new Property(this, "S", new String[0]));
		ret.add(new Property(this, "O", new String[0]));
		ret.add(new Property(this, "G", new String[0]));
		ret.add(new Property(this, "R", new String[0]));
		ret.add(new Property(this, "member", new String[0]));
		ret.add(new Property(this, "owner", new String[0]));
		ret.add(new Property(this, "group", new String[0]));
		ret.add(new Property(this, "ownerRight", new String[0]));
		ret.add(new Property(this, "groupRight", new String[0]));
		ret.add(new Property(this, "otherRight", new String[0]));
		ret.add(new Property(this, "sizeof", new String[0]));
		ret.addAll(super.getMeasurables());
		return ret;
	}
	
	private int getNonHomomorphicSize(Set<String> set){
		int ret = 0;
		for (String s : set){
			ret += numSubs(s);
		}
		return ret;
	}
	
	private int getNonHomomorphicSize(HashMap<String, HashSet<String>> map){
		int ret = 0;
		for (String k : map.keySet()){
			for (String v : map.get(k)){
				ret += numSubs(k) + numSubs(v);
			}
		}
		return ret;
	}
	
	private int getNonHomomoprhicSize(HashMap<String, String> map){
		int ret = 0;
		for (String k : map.keySet()){
			String v = map.get(k);
			ret += numSubs(k) + numSubs(v);
		}
		return ret;
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
		case "R":
			return R;
		case "member":
			return member;
		case "owner":
			return owner;
		case "group":
			return group;
		case "ownerRight":
			return ownerRight;
		case "groupRight":
			return groupRight;
		case "otherRight":
			return otherRight;
		case "sizeof":
			switch(p.params.get(0)) {
			case "S":
				return getNonHomomorphicSize(S);
			case "O":
				return getNonHomomorphicSize(O);
			case "G":
				return getNonHomomorphicSize(G);
			case "R":
				return getNonHomomorphicSize(R);
			case "member":
				return getNonHomomorphicSize(member);
			case "owner":
				return getNonHomomoprhicSize(owner);
			case "group":
				return getNonHomomoprhicSize(group);
			case "ownerRight":
				return 2 * ownerRight.size();
			case "groupRight":
				return 2 * groupRight.size();
			case "otherRight":
				return 2 * otherRight.size();
			case "ALL":
				return getNonHomomorphicSize(S) + getNonHomomorphicSize(O) +
						getNonHomomorphicSize(G) + getNonHomomorphicSize(R) + 
						getNonHomomorphicSize(member) + getNonHomomoprhicSize(owner) +
						getNonHomomoprhicSize(group) + 2 * ownerRight.size() + 
						2 * groupRight.size() + 2 * otherRight.size();
			}
		}
		throw new InvalidMeasureException(p);
	}

}
