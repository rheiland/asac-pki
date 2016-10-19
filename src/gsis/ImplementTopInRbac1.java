package gsis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import ac.Rbac1;
import ac.RbacState.RolePair;
import ac.Rbac1State;
import base.State;
import base.Action;
import base.IAccessAC;
import base.Implementation;
import base.Simulation;
import base.WorkloadState;

/**
 * Put Top into RBAC4 (RBAC with role hierarchy)
 *
 */
public class ImplementTopInRbac1 extends Implementation {
	@Override
	public void init(WorkloadState ws) {
		wScheme = new GsisTop(ws);
		scheme = new Rbac1(stateMap(ws));
	}

	public static String schemeName() {
		return "Rbac1";
	}

	@Override
	public State stateMap(WorkloadState ws) {
		Rbac1State rs = new Rbac1State();
		GsisTopState top = (GsisTopState) ws;

		List<String> S = top.getS().getList();
		List<String> G = top.getG().getList();
		List<String> O = top.getO().getList();

		for(String s : S) {
			rs.addUser(s);
		}

		for(String g: G) {
			rs.addRole(g);
		}

		for(String o: O) {
			rs.addPermission(o);
		}

		HashMap<Integer, Object> records = new HashMap<Integer, Object>();
		List<Integer> timeorder = new ArrayList<Integer>();

		for(SGPair sg : top.getStrictJoins_K().getList()) {
			for(Integer t : top.getStrictJoins_V(sg).getList()) {
				records.put(t, sg);
				timeorder.add(t);
			}
		}

		for(SGPair sg : top.getStrictLeaves_K().getList()) {
			for(Integer t : top.getStrictLeaves_V(sg).getList()) {
				records.put(t, sg);
				timeorder.add(t);
			}
		}

		for(OGPair og : top.getStrictAdd_K().getList()) {
			for(Integer t : top.getStrictAdd_V(og).getList()) {
				records.put(t, og);
				timeorder.add(t);
			}
		}

		for(OGPair og : top.getStrictRemove_K().getList()) {
			for(Integer t : top.getStrictRemove_V(og).getList()) {
				records.put(t, og);
				timeorder.add(t);
			}
		}

		Collections.sort(timeorder);

		for(Integer t : timeorder) {
			Object o = records.get(t);
			if(o instanceof SGPair) {
				SGPair sg = (SGPair) o;
				if(top.getStrictJoins_V(sg).contains(t)) {
					processJoin(top, rs, sg.subject, sg.group);
				} else {
					processLeave(top, rs, sg.subject, sg.group);
				}
			} else if(o instanceof OGPair) {
				OGPair og = (OGPair) o;
				if(top.getStrictAdd_V(og).contains(t)) {
					processAdd(top, rs, og.object, og.group);
				} else {
					processRemove(top, rs, og.object, og.group);
				}
			} else {
				base.SimLogger.log(Level.SEVERE, "An object in records is neither SGPair nor OGPair");
				throw new RuntimeException();
			}
		}

		//
		return rs;
	}

	private void processJoin(GsisTopState top, Rbac1State rs, String s, String g) {
		String newRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		String oldBottom = findBottom(g, rs);
		rs.addRole(newRole);
		rs.addInheritance(oldBottom, newRole);
		rs.associateRoleToUser(s, newRole);
	}

	private void processJoinCmd(String starter, String s, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;

		String newRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		String oldBottom = findBottom(g, rs);
		rbac.action(new Action("addR", starter, newRole));
		rbac.action(new Action("addHierarchy", starter, oldBottom, newRole));
		rbac.action(new Action("assignUser", starter, s, newRole));
	}

	private void processLeave(GsisTopState top, Rbac1State rs, String s, String g) {
		HashSet<String> assignedRoles = findUser(s, g, rs, new HashSet<String>());
		for(String assignedRole : assignedRoles) {
			rs.deassociateRoleToUser(s, assignedRole);
		}
	}

	private void processLeaveCmd(String starter, String s, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;

		HashSet<String> assignedRoles = findUser(s, g, rs, new HashSet<String>());
		for(String assignedRole : assignedRoles) {
			rbac.action(new Action("revokeUser", starter, s, assignedRole));
		}
	}

	private void processAdd(GsisTopState top, Rbac1State rs, String o, String g) {
		String bottom = findBottom(g, rs);
		rs.associateRoleToPermission(o, bottom);
	}

	private void processAddCmd(String starter, String o, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;

		String bottom = findBottom(g, rs);
		rbac.action(new Action("assignPermission", starter, bottom, o));
	}

	private void processRemove(GsisTopState top, Rbac1State rs, String o, String g) {
		HashSet<String> assignedRoles = findPerm(o, g, rs, new HashSet<String>());
		for(String assignedRole : assignedRoles) {
			rs.deassociateRoleToPermission(o, assignedRole);
		}
	}

	private void processRemoveCmd(String starter, String o, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;

		HashSet<String> assignedRoles = findPerm(o, g, rs, new HashSet<String>());
		for(String assignedRole : assignedRoles) {
			rbac.action(new Action("revokePermission", starter, assignedRole, o));
		}
	}

	private HashSet<String> findUser(String u, String r, Rbac1State rs, HashSet<String> assignedRoles) {
		if(rs.getUA_K().contains(u) && rs.getUA_V(u).contains(r)) {
			assignedRoles.add(r);
		}
		for(RolePair rh : rs.getRH().getList()) {
			if(rh.seniorRole.equals(r)) {
				return findUser(u, rh.juniorRole, rs, assignedRoles);
			}
		}
		return assignedRoles;
	}

	private HashSet<String> findPerm(String p, String r, Rbac1State rs, HashSet<String> assignedRoles) {
		if(rs.getPA_K().contains(p) && rs.getPA_V(p).contains(r)) {
			assignedRoles.add(r);
		}
		for(RolePair rh : rs.getRH().getList()) {
			if(rh.seniorRole.equals(r)) {
				return findPerm(p, rh.juniorRole, rs, assignedRoles);
			}
		}
		return assignedRoles;
	}

	/**
	 * Find the junior most role in the group.
	 * Return g if g is the most junior.
	 * @param g the group we are searching
	 * @param rs the rbac state
	 * @return
	 */
	private String findBottom(String g, Rbac1State rs) {
		for(RolePair rh : rs.getRH().getList()) {
			if(g.equals(rh.seniorRole)) {
				return findBottom(rh.juniorRole, rs);
			}
		}
		return g;
	}

	@Override
	public void action(Action a) {
		super.action(a);
		if(!GsisTop.actorToGSIS.containsKey(a.name)) {
			return;
		}
		Rbac1 r = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) scheme.state;
		switch(GsisTop.actorToGSIS.get(a.name)) {
		case "delO":
			r.action(new Action("delP", a.params));
			break;
		case "delG":
			HashSet<String> chain = rs.findRoleChain(a.params[1]);
			for(String r2 : chain) {
				r.action(new Action("delR", a.params[0], r2));
			}
			break;
		case "SAdd":
			processAddCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "SRemove":
			processRemoveCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "addG":
			r.action(new Action("addR", a.params));
			break;
		case "SLeave":
			processLeaveCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "SJoin":
			processJoinCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "addO":
			r.action(new Action("addP", a.params));
			break;
		case "auth": {
			IAccessAC<String> R1 = rs.getR();
			while(R1.hasNext()) {
				String r1 = R1.next();
				IAccessAC<String> R2 = rs.getR();
				while(R2.hasNext()) {
					String r2 = R2.next();

					Action urAction = new Action(true, "UR", a.params[0], a.params[1], r1);
					Action paAction = new Action(true, "PA", a.params[0], r2, a.params[2]);
					r.query(urAction);
					boolean urResult = r.lastQueryResult;
					r.query(paAction);
					boolean paResult = r.lastQueryResult;
					if(urResult && paResult) {
						//Senior(g, r1)
						Action gAction = new Action(true, "Senior", a.params[0], a.params[3], r1);
						r.query(gAction);
						boolean gResult = r.lastQueryResult;
						if(gResult) {
							//check whether r1 == r2 or Senior(r1, r2)
							if(r1.equals(r2)) {
								r.lastQueryResult = true;
								return;
							}

							Action seniorAction = new Action(true, "Senior", a.params[0], r1, r2);
							r.query(seniorAction);
							boolean seniorResult = r.lastQueryResult;
							if(seniorResult) {
								r.lastQueryResult = true;
								return;
							}
						}
					}
				}
			}
			r.lastQueryResult = false;
			break;
		}
		}

	}
}
