package ac;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import base.IAccessACH;
import base.State;
import base.IAccessAC;
import base.InvalidMeasureException;
import base.Property;
import static base.Implementation.numSubs;

public class Rbac0State extends State {
	// user -> roles
	protected Map<String, Set<String>> UA = new LinkedHashMap<String, Set<String>>();
	// role -> messages
	protected Map<String, Set<String>> PA = new LinkedHashMap<String, Set<String>>();
	protected HashSet<String> U = new HashSet<String>();
	protected HashSet<String> P = new HashSet<String>();
	protected HashSet<String> R = new HashSet<String>();
	@Override
	public Set<Property> getMeasurables() {
		HashSet<Property> ret = new HashSet<Property>();
		ret.add(new Property(this, "UA", new String[0]));
		ret.add(new Property(this, "PA", new String[0]));
		ret.add(new Property(this, "U", new String[0]));
		ret.add(new Property(this, "P", new String[0]));
		ret.add(new Property(this, "R", new String[0]));
		ret.add(new Property(this, "RH", new String[0]));
		ret.add(new Property(this, "sizeof", new String[0]));
		ret.addAll(super.getMeasurables());
		return ret;
	}
	@Override
	public boolean isMeasurable(Property p) {
		Property qTest = new Property(this, "__test", new String[0]);
		if(!qTest.matchQualifier(p)) {
			return false;
		}
		switch(p.name) {
		case "UA":
		case "PA":
		case "U":
		case "P":
		case "R":
		case "RH":
			return true;
		case "sizeof":
			switch(p.params.get(0)) {
			case "UA":
			case "PA":
			case "U":
			case "P":
			case "R":
			case "RH":
			case "ALL":
				return true;
			}
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
		case "UA":
			return UA;
		case "PA":
			return PA;
		case "U":
			return U;
		case "P":
			return P;
		case "R":
			return R;
		case "sizeof":
			switch(p.params.get(0)) {
			case "UA":
				return getUrSize(UA);
			case "PA":
				return getPaSize(PA);
			case "U":
				return U.size();
			case "P":
				return P.size();
			case "R":
				return getRSize(R);
			case "ALL":
				return getUrSize(UA) + getPaSize(PA) + U.size() + P.size() + getRSize(R);
			}
		}
		throw new InvalidMeasureException(p);
	}

	int getUrSize(Map<String, Set<String>> UR) {
		int size = 0;
		for(String u : UR.keySet()) {
			Set<String> roles = UR.get(u);
			for(String role : roles) {
				size += 1 + numSubs(role);
			}
		}
		return size;
	}

	int getPaSize(Map<String, Set<String>> PA) {
		int size = 0;
		for(String p : PA.keySet()) {
			Set<String> roles = PA.get(p);
			for(String role : roles) {
				size += 1 + numSubs(role);
			}
		}
		return size;
	}

	int getRSize(Set<String> R) {
		int size = 0;
		for(String role : R) {
			size += numSubs(role);
		}
		return size;
	}
	@Override
	public State createCopy() {
		Rbac0State ret = new Rbac0State();
		for(String u : U) {
			ret.addUser(u);
		}
		for(String r : R) {
			ret.addRole(r);
		}
		for(String p : P) {
			ret.addPermission(p);
		}
		for(String u : UA.keySet()) {
			for(String r : UA.get(u)) {
				ret.associateRoleToUser(u, r);
			}
		}
		for(String p : PA.keySet()) {
			for(String r : PA.get(p)) {
				ret.associateRoleToPermission(p, r);
			}
		}
		return ret;
	}

	public void addUser(String u) {
		if(U.add(u)) incrementAccessIO(0, 1);
	}

	public void addPermission(String p) {
		if(P.add(p)) incrementAccessIO(0, 1);
	}

	public void delPermission(String p) {
		if(P.contains(p)) {
			P.remove(p);
			incrementAccessIO(0, 1);
			if(PA.containsKey(p)) {
				incrementAccessIO(0, 1 + getNonHomomorphicSize(PA.get(p)));
				PA.remove(p);
			}
		}
	}

	public void addRole(String r) {
		if(R.add(r)) incrementAccessIO(0, numSubs(r));
	}

	public void delRole(String r) {
		if(R.remove(r)) incrementAccessIO(0, numSubs(r));
		for(String u : UA.keySet()) {
			if (UA.get(u).remove(r)) {
				incrementAccessIO(0, 1 + numSubs(r));
			}
		}
		for(String p : PA.keySet()) {
			boolean success = PA.get(p).remove(r);
			if (success){
				incrementAccessIO(0, 1 + numSubs(r));
			}
		}
	}

	public void associateRoleToUser(String u, String r) {
		if(U.add(u)) incrementAccessIO(0, 1);
		if(!UA.containsKey(u)) {
			UA.put(u, new HashSet<String>());
		}
		if(UA.get(u).add(r)) incrementAccessIO(0, 1 + numSubs(r));
	}

	public void associateRoleToPermission(String p, String r) {
		if(P.add(p)) incrementAccessIO(0, 1);
		if(!PA.containsKey(p)) {
			PA.put(p, new HashSet<String>());
		}
		if(PA.get(p).add(r)) incrementAccessIO(0, 1 + numSubs(r));
	}

	public void deassociateRoleToUser(String u, String r) {
		if(UA.containsKey(u)) {
			if(UA.get(u).remove(r)) incrementAccessIO(0, 1 + numSubs(r));
		}
	}

	public void deassociateRoleToPermission(String p, String r) {
		if(PA.containsKey(p)) {
			if(PA.get(p).remove(r))	incrementAccessIO(0, 1 + numSubs(r));
		}
	}
	
	private int getNonHomomorphicSize(Set<String> set){
		int ret = 0;
		for (String s : set){
			ret += numSubs(s);
		}
		return ret;
	}

	public IAccessAC<String> getUA_K() {
		return new IAccessAC<String>(UA.keySet(), this, 0);
	}

	public IAccessAC<String> getUA_V(String u) {
		return new IAccessACH(UA.get(u), this, 1);
	}

	public IAccessAC<String> getPA_K() {
		return new IAccessAC<String>(PA.keySet(), this, 0);
	}

	public IAccessAC<String> getPA_V(String p) {
		return new IAccessACH(PA.get(p), this, 1);
	}

	public IAccessAC<String> getU() {
		return new IAccessAC<String>(U, this, 0);
	}

	public IAccessAC<String> getP() {
		return new IAccessAC<String>(P, this, 0);
	}

	public IAccessAC<String> getR() {
		return new IAccessACH(R, this, 0);
	}
}
