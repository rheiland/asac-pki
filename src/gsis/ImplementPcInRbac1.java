package gsis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import ac.Rbac1;
import ac.Rbac1State;
import ac.RbacState.RolePair;
import base.State;
import base.Action;
import base.IAccessAC;
import base.Implementation;
import base.SimLogger;
import base.Simulation;
import base.WorkloadState;

public class ImplementPcInRbac1 extends Implementation {

	@Override
	public void init(WorkloadState ws) {
		wScheme = new GsisPc(ws);
		scheme = new ac.Rbac1(stateMap(ws));
	}

	public static String schemeName() {
		return "Rbac1";
	}

	@Override
	public State stateMap(WorkloadState ws) {
		Rbac1State rs = new Rbac1State();
		GsisPcState pc = (GsisPcState) ws;

		List<String> S = pc.getS().getList();
		List<String> G = pc.getG().getList();
		List<String> O = pc.getO().getList();

		for(String s : S) {
			rs.addUser(s);
		}

		for(String g: G) {
			initGroup(pc, g, rs);
		}

		for(String o: O) {
			rs.addPermission(o);
		}

		HashMap<Integer, Object> records = new HashMap<Integer, Object>();
		List<Integer> timeorder = new ArrayList<Integer>();

		for(SGPair sg : pc.getLiberalJoins_K().getList()) {
			for(Integer t : pc.getLiberalJoins_V(sg).getList()) {
				records.put(t, sg);
				timeorder.add(t);
			}
		}

		for(SGPair sg : pc.getLiberalLeaves_K().getList()) {
			for(Integer t : pc.getLiberalLeaves_V(sg).getList()) {
				records.put(t, sg);
				timeorder.add(t);
			}
		}

		for(OGPair og : pc.getLiberalAdd_K().getList()) {
			for(Integer t : pc.getLiberalAdd_V(og).getList()) {
				records.put(t, og);
				timeorder.add(t);
			}
		}

		for(SGPair sg : pc.getStrictJoins_K().getList()) {
			for(Integer t : pc.getStrictJoins_V(sg).getList()) {
				records.put(t, sg);
				timeorder.add(t);
			}
		}

		for(SGPair sg : pc.getStrictLeaves_K().getList()) {
			for(Integer t : pc.getStrictLeaves_V(sg).getList()) {
				records.put(t, sg);
				timeorder.add(t);
			}
		}

		Collections.sort(timeorder);

		for(Integer t : timeorder) {
			Object o = records.get(t);
			if(o instanceof SGPair) {
				SGPair sg = (SGPair) o;
				if(pc.getLiberalJoins_V(sg).contains(t)) {
					processLJoin(pc, rs, sg.subject, sg.group);
				} else if(pc.getLiberalLeaves_V(sg).contains(t)) {
					processLLeave(pc, rs, sg.subject, sg.group);
				} else if(pc.getStrictJoins_V(sg).contains(t)) {
					processSJoin(pc, rs, sg.subject, sg.group);
				} else if(pc.getStrictLeaves_V(sg).contains(t)) {
					processSLeave(pc, rs, sg.subject, sg.group);
				} else {
					base.SimLogger.log(Level.SEVERE, "An sg test is not join nor leave");
					throw new RuntimeException();
				}
			} else if(o instanceof OGPair) {
				OGPair og = (OGPair) o;
				if(pc.getLiberalAdd_V(og).contains(t)) {
					processAdd(pc, rs, og.object, og.group);
				} else {
					base.SimLogger.log(Level.SEVERE, "An og test is not add");
					throw new RuntimeException();
				}
			} else {
				base.SimLogger.log(Level.SEVERE, "An object in records is neither SGPair nor OGPair");
				throw new RuntimeException();
			}
		}

		return rs;
	}

	private void processSJoin(GsisPcState pc, Rbac1State rs, String s, String g) {
		String newBottom = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		String oldBottom = findBottom(g, rs);
		rs.addRole(newBottom);
		rs.addInheritance(oldBottom, newBottom);
		rs.associateRoleToUser(s, newBottom);
	}

	private void processSJoinCmd(String starter, String s, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;

		String newBottom = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		String oldBottom = findBottom(g, rs);
		rbac.action(new Action("addR", starter, newBottom));
		rbac.action(new Action("addHierarchy", starter, oldBottom, newBottom));
		rbac.action(new Action("assignUser", starter, s, newBottom));
	}

	private void processSLeave(GsisPcState pc, Rbac1State rs, String s, String g) {
		leaveDown(s, g, rs);
		leaveOrphans(s, g, rs);
	}

	private void processSLeaveCmd(String starter, String s, String g) {
		leaveDownCmd(starter, s, g);
		leaveOrphansCmd(starter, s, g);
	}

	private void processLJoin(GsisPcState pc, Rbac1State rs, String s, String g) {
		rs.associateRoleToUser(s, g);
	}

	private void processLJoinCmd(String starter, String s, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		//Rbac1State rs = (Rbac1State) rbac.state;

		rbac.action(new Action("assignUser", starter, s, g));
	}

	private void processLLeave(GsisPcState pc, Rbac1State rs, String s, String g) {
		String orphan = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		rs.addRole(orphan);
		HashSet<String> permissions = permsInChain(s, g, rs);
		String top = findTop(g, rs);
		for(String permission : permissions) {
			rs.associateRoleToPermission(permission, orphan);
		}
		rs.associateRoleToUser(s, orphan);
		rs.addInheritance(top, orphan);
		leaveDown(s, g, rs);
	}

	private void processLLeaveCmd(String starter, String s, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;

		String orphan = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		rbac.action(new Action("addR", starter, orphan));
		HashSet<String> permissions = permsInChain(s, g, rs);
		String top = findTop(g, rs);
		for(String permission : permissions) {
			rbac.action(new Action("assignPermission", starter, orphan, permission));
		}
		rbac.action(new Action("assignUser", starter, s, orphan));
		rbac.action(new Action("addHierarchy", starter, top, orphan));
		leaveDownCmd(starter, s, g);
	}

	private void processAdd(GsisPcState pc, Rbac1State rs, String o, String g) {
		String bottom = findBottom(g, rs);
		rs.associateRoleToPermission(o, bottom);
	}

	private void processAddCmd(String starter, String o, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;
		String bottom = findBottom(g, rs);
		rbac.action(new Action("assignPermission", starter, bottom, o));
	}

	private void initGroup(GsisPcState pc , String g, Rbac1State rs) {
		rs.addRole(g);
		String topRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		rs.addRole(topRole);
		String bottomRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		rs.addRole(bottomRole);
		rs.addInheritance(topRole, g);
		rs.addInheritance(g, bottomRole);
	}

	private void initGroupCmd(String starter, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;

		rbac.action(new Action("addR", starter, g));
		String topRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		rbac.action(new Action("addR", starter, topRole));
		String bottomRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		rbac.action(new Action("addR", starter, bottomRole));
		rbac.action(new Action("addHierarchy", starter, topRole, g));
		rbac.action(new Action("addHierarchy", starter, g, bottomRole));
	}

	private String findTop(String r, Rbac1State rs) {
		for(RolePair roles : rs.getRH().getList()) {
			if(roles.juniorRole.equals(r)) {
				return findTop(roles.seniorRole, rs);
			}
		}
		return r;
	}

	private String findBottom(String r, Rbac1State rs) {
		for(RolePair roles : rs.getRH().getList()) {
			if(roles.seniorRole.equals(r)) {
				return findTop(roles.juniorRole, rs);
			}
		}
		return r;
	}

	private void leaveDown(String u, String r, Rbac1State rs) {
		rs.deassociateRoleToUser(u, r);
		for(RolePair roles : rs.getRH().getList()) {
			if(roles.seniorRole.equals(r)) {
				leaveDown(u, roles.juniorRole, rs);
			}
		}
	}

	private void leaveDownCmd(String starter, String u, String r) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;
		rbac.action(new Action("revokeUser", starter, u, r));
		for(RolePair roles : rs.getRH().getList()) {
			if(roles.seniorRole.equals(r)) {
				leaveDown(u, roles.juniorRole, rs);
			}
		}
	}

	private void leaveOrphans(String u, String g, Rbac1State rs) {
		for(RolePair roles1 : rs.getRH().getList()) {
			if(roles1.juniorRole.equals(g)) {
				String head = roles1.seniorRole;
				for(RolePair roles2 : rs.getRH().getList()) {
					if(roles2.seniorRole.equals(head)) {
						String orphan = roles2.juniorRole;
						rs.deassociateRoleToUser(u, orphan);
					}
				}
			}
		}
	}

	private void leaveOrphansCmd(String starter, String u, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;
		for(RolePair roles1 : rs.getRH().getList()) {
			if(roles1.juniorRole.equals(g)) {
				String head = roles1.seniorRole;
				for(RolePair roles2 : rs.getRH().getList()) {
					if(roles2.seniorRole.equals(head)) {
						String orphan = roles2.juniorRole;
						rbac.action(new Action("revokeUser", starter, u, orphan));
					}
				}
			}
		}
	}

	private HashSet<String> permsInChain(String u, String r, Rbac1State rs) {
		if(rs.getUA_K().contains(u) && rs.getUA_V(u).contains(r)) {
			permsBelow(r, rs, new HashSet<String>());
		} else {
			for(RolePair roles : rs.getRH().getList()) {
				if(roles.seniorRole.equals(r)) {
					return permsInChain(u, roles.juniorRole, rs);
				}
			}
		}
		return new HashSet<String>();
	}

	private HashSet<String> permsBelow(String r, Rbac1State rs, HashSet<String> perms) {
		for(String p : rs.getPA_K().getList()) {
			if(rs.getPA_V(p).contains(r)) {
				perms.add(p);
			}
		}
		for(RolePair roles : rs.getRH().getList()) {
			if(roles.seniorRole.equals(r)) {
				return permsBelow(roles.juniorRole, rs, perms);
			}
		}
		return perms;
	}

	@Override
	public void action(Action a) {
		super.action(a);
		if(!GsisPc.actorToGSIS.containsKey(a.name)) {
			return;
		}
		Rbac1 r = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) scheme.state;
		switch(GsisPc.actorToGSIS.get(a.name)) {
		case "delG":
			HashSet<String> chain = rs.findRoleChain(a.params[1]);
			for(String r2 : chain) {
				r.action(new Action("delR", a.params[0], r2));
			}
			break;
		case "delO":
			r.action(new Action("delP", a.params));
			break;
		case "LAdd":
			processAddCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "SJoin":
			processSJoinCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "SLeave":
			processSLeaveCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "addG":
			initGroupCmd(a.params[0], a.params[1]);
			break;
		case "LLeave":
			processLLeaveCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "LJoin":
			processLJoinCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "addO":
			r.action(new Action("addP", a.params));
			break;
		case "auth": {
			// Cripple until we can do it well
			IAccessAC<String> R1 = rs.getR();
			while(R1.hasNext()) {
				String r1 = R1.next();
			}
			break;
		}
		default:
			SimLogger.log(Level.WARNING, getClass().getName() + ": Cannot execute action " + a);
		}
	}


}
