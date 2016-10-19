package gsis;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import base.IAccessW;
import base.InvalidMeasureException;
import base.Property;
import base.WorkloadState;

/**
 * This represents an RBAC state.
 *
 * Be careful with U. It needs to match with the actor machine's actor names.
 *
 */
public class RbacState extends WorkloadState implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -7626250799222882639L;
	// user -> roles
	protected Map<String, Set<String>> UA = new LinkedHashMap<String, Set<String>>();
	// role -> messages
	protected Map<String, Set<String>> PA = new LinkedHashMap<String, Set<String>>();
	protected HashSet<String> U = new HashSet<String>();
	protected HashSet<String> P = new HashSet<String>();
	protected HashSet<String> R = new HashSet<String>();
	protected HashSet<RolePair> RH = new HashSet<RolePair>();
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
		case "RH":
			return RH;
		case "sizeof":
			switch(p.params.get(0)) {
			case "UA":
				return (int) getMap2Size(UA);
			case "PA":
				return (int) getMap2Size(PA);
			case "U":
				return U.size();
			case "P":
				return P.size();
			case "R":
				return R.size();
			case "RH":
				return 2 * RH.size();
			}
		}
		throw new InvalidMeasureException(p);
	}

	protected <T, K> int getMap2Size(Map<T, Set<K>> map) {
		int size = 0;
		for(Object o : map.keySet()) {
			size += 2 * map.get(o).size();
		}
		return size;
	}
	@Override
	public WorkloadState createCopy() {
		RbacState ret = new RbacState();
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
		for(RolePair pair : RH) {
			ret.addInheritance(pair.seniorRole, pair.juniorRole);
		}
		return ret;
	}

	public void addInheritance(String seniorRole, String juniorRole) {
		RolePair pair = new RolePair(seniorRole, juniorRole);
		if(!RH.contains(pair)) {
			RH.add(pair);
			incrementAccessIO(0, 2);
		}
	}

	public void addUser(String u) {
		if(!U.contains(u)) {
			U.add(u);
			incrementAccessIO(0, 1);
		}
	}

	public void addPermission(String p) {
		if(!P.contains(p)) {
			P.add(p);
			incrementAccessIO(0, 1);
		}
	}

	public void delPermission(String p) {
		if(P.contains(p)) {
			P.remove(p);
			incrementAccessIO(0, 1);
		}
	}

	public void addRole(String r) {
		if(!R.contains(r)) {
			R.add(r);
			incrementAccessIO(0, 1);
		}
	}

	public void associateRoleToUser(String u, String r) {
		if(!UA.containsKey(u)) {
			UA.put(u, new HashSet<String>());
		}
		if(!UA.get(u).contains(r)) {
			UA.get(u).add(r);
			incrementAccessIO(0, 2);
		}
	}

	public void associateRoleToPermission(String p, String r) {
		if(!P.contains(p)) {
			P.add(p);
			incrementAccessIO(0, 1);
		}
		if(!PA.containsKey(p)) {
			PA.put(p, new HashSet<String>());
		}
		if(!PA.get(p).contains(r)) {
			PA.get(p).add(r);
			incrementAccessIO(0, 2);
		}
	}

	public IAccessW<String> getUA_K() {
		return new IAccessW<String>(UA.keySet(), this, 0);
	}

	public IAccessW<String> getUA_V(String u) {
		return new IAccessW<String>(UA.get(u), this, 1);
	}

	public IAccessW<String> getPA_K() {
		return new IAccessW<String>(PA.keySet(), this, 0);
	}

	public IAccessW<String> getPA_V(String p) {
		return new IAccessW<String>(PA.get(p), this, 1);
	}

	public IAccessW<String> getU() {
		return new IAccessW<String>(U, this, 0);
	}

	public IAccessW<String> getP() {
		return new IAccessW<String>(P, this, 0);
	}

	public IAccessW<String> getR() {
		return new IAccessW<String>(R, this, 0);
	}

	public IAccessW<RolePair> getRH() {
		return new IAccessW<RolePair>(RH, this, 1);
	}

	public class RolePair {
		public String seniorRole;
		public String juniorRole;

		public RolePair(String seniorRole, String juniorRole) {
			this.seniorRole = seniorRole;
			this.juniorRole = juniorRole;
		}

		public String toString() {
			return "(" + seniorRole + " >= " + juniorRole + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
			         + ((juniorRole == null) ? 0 : juniorRole.hashCode());
			result = prime * result
			         + ((seniorRole == null) ? 0 : seniorRole.hashCode());
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
			RolePair other = (RolePair) obj;
			if(!getOuterType().equals(other.getOuterType()))
				return false;
			if(juniorRole == null) {
				if(other.juniorRole != null)
					return false;
			} else if(!juniorRole.equals(other.juniorRole))
				return false;
			if(seniorRole == null) {
				if(other.seniorRole != null)
					return false;
			} else if(!seniorRole.equals(other.seniorRole))
				return false;
			return true;
		}

		private RbacState getOuterType() {
			return RbacState.this;
		}
	}

	public void deleteRole(String r) {
		R.remove(r);
		incrementAccessIO(0, 1);
	}

	public void deleteInheritence(String seniorRole, String juniorRole) {
		for(RolePair pair: RH) {
			if(pair.seniorRole.equals(seniorRole) && pair.juniorRole.equals(juniorRole)) {
				incrementAccessIO(0, 2);
				RH.remove(pair);
			}
		}
	}

	public void deassociateRoleToUser(String u, String r) {
		if(!UA.containsKey(u)) {
			return;
		}
		UA.get(u).remove(r);
		incrementAccessIO(0, 2);
	}

	public void deassociateRoleToPermission(String p, String r) {
		if(!PA.containsKey(p)) {
			return;
		}
		PA.get(p).remove(r);
		incrementAccessIO(0, 2);
	}

	public HashSet<String> findAllSeniors(String g, HashSet<String> ret) {
		for(RolePair rg : RH) {
			String r = rg.seniorRole;
			if(rg.juniorRole.equals(g)) {
				ret.add(r);
				return findAllSeniors(r, ret);
			}
		}

		return ret;
	}

	public HashSet<String> findAllJuniors(String g, HashSet<String> ret) {
		for(RolePair rg : RH) {
			incrementAccessIO(0, 1);
			String r = rg.juniorRole;
			if(rg.seniorRole.equals(g)) {
				ret.add(r);
				return findAllSeniors(r, ret);
			}
		}

		return ret;
	}

	public HashSet<String> findRoleChain(String r) {
		HashSet<String> ret = new HashSet<String>();
		ret = findAllSeniors(r, ret);
		ret = findAllJuniors(r, ret);
		return ret;
	}
}
