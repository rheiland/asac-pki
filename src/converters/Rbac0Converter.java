package converters;

import ac.Rbac0State;
import ac.RbacState.RolePair;
import base.State;
import base.WorkloadState;
import base.StateToWorkloadStateConverter;

/**
 * Conversion for rbac without role hierarchy
 * @author yechen
 *
 */
public class Rbac0Converter implements StateToWorkloadStateConverter {

	@Override
	public WorkloadState toWorkloadState(State in) {
		Rbac0State ac = (Rbac0State) in;
		gsis.Rbac0State ret = new gsis.Rbac0State();
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
		return ret;
	}

}
