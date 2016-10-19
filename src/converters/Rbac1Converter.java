package converters;

import ac.Rbac1State;
import ac.RbacState.RolePair;
import base.StateToWorkloadStateConverter;
import base.State;
import base.WorkloadState;

/**
 * Convert AC Rbac4 with RH to Workload Rbac4 with RH
 * @author yechen
 *
 */
public class Rbac1Converter implements StateToWorkloadStateConverter {

	@Override
	public WorkloadState toWorkloadState(State in) {
		Rbac1State ac = (Rbac1State) in;
		gsis.Rbac1State ret = new gsis.Rbac1State();
		for(String u : ac.getU().getList()) {
			ret.addUser(u);
		}
		for(String r : ac.getR().getList()) {
			ret.addRole(r);
		}
		for(String p : ac.getP().getList()) {
			ret.addPermission(p);
		}

		for(String u : ac.getUA_K().getList()) {
			for(String r : ac.getUA_V(u).getList()) {
				ret.associateRoleToUser(u, r);
			}
		}
		for(String p : ac.getPA_K().getList()) {
			for(String r :ac.getPA_V(p).getList()) {
				ret.associateRoleToPermission(p, r);
			}
		}
		for(RolePair pair : ac.getRH().getList()) {
			ret.addInheritance(pair.seniorRole, pair.juniorRole);
		}
		return ret;
	}

}
