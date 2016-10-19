package gsis;

import java.util.HashSet;
import java.util.Set;

import base.WorkloadState;
import base.InvalidMeasureException;
import base.Property;

/**
 * Plain RBAC with RH
 * @author yechen
 *
 */
public class Rbac1State extends RbacState {
	/**
	 *
	 */
	private static final long serialVersionUID = 1595361546053072381L;

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
			case "ALL":
				return  getMap2Size(UA) + getMap2Size(PA) + U.size() + P.size() + R.size() + 2 * RH.size();
			case "ExtALL":
				return 0;
			}
		}
		throw new InvalidMeasureException(p);
	}

	@Override
	public boolean isMeasurable(Property p) {
		Set<Property> props = getMeasurables();
		for(Property pp : props) {
			if(pp.match(p)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Property> getMeasurables() {
		HashSet<Property> ret = new HashSet<Property>();
		ret.add(new Property(this, "timestamp", new String[0]));
		ret.addAll(super.getMeasurables());
		return ret;
	}

	@Override
	public WorkloadState createCopy() {
		Rbac1State ret = new Rbac1State();
		ret.U.addAll(U);
		ret.R.addAll(R);
		ret.P.addAll(P);
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

}
