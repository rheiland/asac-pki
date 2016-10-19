package gsis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import ac.Rbac1;
import ac.Rbac1State;
import ac.RbacState;
import ac.RbacState.RolePair;
import base.BiAccess;
import base.State;
import base.Action;
import base.IAccessAC;
import base.Implementation;
import base.SimLogger;
import base.Simulation;
import base.WorkloadState;

public class ImplementBottomInRbac1 extends Implementation {

	@Override
	public void init(WorkloadState ws) {
		wScheme = new GsisBottom(ws);
		scheme = new ac.Rbac1(stateMap(ws));
	}

	public static String schemeName() {
		return "Rbac1";
	}

	@Override
	public State stateMap(WorkloadState ws) {
		Rbac1State rs = new Rbac1State();
		GsisBottomState bottom = (GsisBottomState) ws;

		bottom.getS().forEach( s -> {
			rs.addUser(s);
		});

		bottom.getG().forEach( g -> {
			initGroup(bottom, g, rs);
		});

		bottom.getO().forEach( o-> {
			rs.addPermission(o);
		});

		//		List<String> S = bottom.getSOld().getList();
		//		List<String> G = bottom.getGOld().getList();
		//		List<String> O = bottom.getOOld().getList();
		//
		//		for(String s : S) {
		//			rs.addUser(s);
		//		}
		//
		//		for(String g: G) {
		//			initGroup(bottom, g, rs);
		//		}
		//
		//		for(String o: O) {
		//			rs.addPermission(o);
		//		}



		HashMap<Integer, Object> records = new HashMap<Integer, Object>();



		bottom.getLiberalJoins().forEach((sg, t) -> {
			records.put(t, sg);
		});

		bottom.getLiberalLeaves().forEach( (sg, t) -> {
			records.put(t, sg);
		});

		//		List<Integer> timeorder = new ArrayList<Integer>();
		//		for(SGPair sg : bottom.getLiberalJoins_K().getList()) {
		//			for(Integer t : bottom.getLiberalJoins_V(sg).getList()) {
		//				records.put(t, sg);
		//				timeorder.add(t);
		//			}
		//		}
		//
		//		for(SGPair sg : bottom.getLiberalLeaves_K().getList()) {
		//			for(Integer t : bottom.getLiberalLeaves_V(sg).getList()) {
		//				records.put(t, sg);
		//				timeorder.add(t);
		//			}
		//		}
		//
		//		for(OGPair og : bottom.getLiberalAdd_K().getList()) {
		//			for(Integer t : bottom.getLiberalAdd_V(og).getList()) {
		//				records.put(t, og);
		//				timeorder.add(t);
		//			}
		//		}
		//
		//		for(OGPair og : bottom.getLiberalRemove_K().getList()) {
		//			for(Integer t : bottom.getLiberalRemove_V(og).getList()) {
		//				records.put(t, og);
		//				timeorder.add(t);
		//			}
		//		}
		//
		//		Collections.sort(timeorder);

		//Note we're using forEachOrdered instead of forEach because we need time ordering to be increasing
		records.entrySet().parallelStream().sorted( (e1, e2) -> e1.getKey().compareTo(e2.getKey())).forEachOrdered( e -> {
			Integer t = e.getKey();
			Object o = e.getValue();
			if (o instanceof SGPair){
				SGPair sg = (SGPair) o;
				if (bottom.getLiberalJoins().exists(sg, t)) {
					processJoin(bottom, rs, sg.subject, sg.group);
				} else if (bottom.getLiberalLeaves().exists(sg, t)){
					processLeave(bottom, rs, sg.subject, sg.group);
				} else {
					base.SimLogger.log(Level.SEVERE, "An sg test is not join nor leave");
					throw new RuntimeException();
				}
			} else if (o instanceof OGPair) {
				OGPair og = (OGPair) o;
				if (bottom.getLiberalAdds().exists(og, t)){
					processAdd(bottom, rs, og.object, og.group);
				} else if(bottom.getLiberalRemoves().exists(og, t)){
					processRemove(bottom, rs, og.object, og.group);
				} else {
					base.SimLogger.log(Level.SEVERE, "An og test is not add or remove");
					throw new RuntimeException();
				}
			} else {
				base.SimLogger.log(Level.SEVERE, "An object in records is neither SGPair nor OGPair");
				throw new RuntimeException();
			}
		});

		//		for(Integer t : timeorder) {
		//			Object o = records.get(t);
		//			if(o instanceof SGPair) {
		//				SGPair sg = (SGPair) o;
		//				if(bottom.getLiberalJoins_V(sg).contains(t)) {
		//					processJoin(bottom, rs, sg.subject, sg.group);
		//				} else if(bottom.getLiberalLeaves_V(sg).contains(t)) {
		//					processLeave(bottom, rs, sg.subject, sg.group);
		//				} else {
		//					base.SimLogger.log(Level.SEVERE, "An sg test is not join nor leave");
		//					throw new RuntimeException();
		//				}
		//			} else if(o instanceof OGPair) {
		//				OGPair og = (OGPair) o;
		//				if(bottom.getLiberalAdd_V(og).contains(t)) {
		//					processAdd(bottom, rs, og.object, og.group);
		//				} else if(bottom.getLiberalRemove_V(og).contains(t)) {
		//					processRemove(bottom, rs, og.object, og.group);
		//				} else {
		//					base.SimLogger.log(Level.SEVERE, "An og test is not add or remove");
		//					throw new RuntimeException();
		//				}
		//			} else {
		//				base.SimLogger.log(Level.SEVERE, "An object in records is neither SGPair nor OGPair");
		//				throw new RuntimeException();
		//			}
		//		}

		return rs;
	}

	private void processJoin(GsisBottomState bottom, Rbac1State rs, String s, String g) {
		String top = findTop(g, rs);
		rs.associateRoleToUser(s, top);
	}

	private void processJoinCmd(String starter, String s, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;

		String top = findTop(g, rs);
		rbac.action(new Action("assignUser", starter, s, top));
	}

	private void processLeave(GsisBottomState bottom, Rbac1State rs, String s, String g) {
		String oldTop = findTop(g, rs);
		HashSet<String> movers = new HashSet<String>();
		if (rs.getUA().exists(s, oldTop)){
			String newRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
			rs.addRole(newRole);
			rs.addInheritance(newRole, oldTop);
			BiAccess<RbacState, String, String> UA = rs.getUA();
			UA.forEach((u, r)-> r.equals(oldTop) && !(u.equals(s)),
					(u, r) -> {
						movers.add(u);
						rs.deassociateRoleToUser(u, oldTop);
					}, 2, 0);
			movers.forEach( mover -> {
				rs.associateRoleToUser(mover, newRole);
			});
		}
		
//		if(rs.getUA_K().contains(s) && rs.getUA_V(s).contains(oldTop)) {
//			String newRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
//			rs.addRole(newRole);
//			rs.addInheritance(newRole, oldTop);
//			for(String x : rs.getUA_K().getList()) {
//				if(rs.getUA_V(x).contains(oldTop) && !(x.equals(s))) {
//					movers.add(x);
//					rs.deassociateRoleToUser(x, oldTop);
//				}
//			}
//			for(String mover : movers) {
//				rs.associateRoleToUser(mover, newRole);
//			}
//		}
	}

	private void processLeaveCmd(String starter, String s, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;

		String oldTop = findTop(g, rs);
		HashSet<String> movers = new HashSet<String>();
		if(rs.getUA_K().contains(s) && rs.getUA_V(s).contains(oldTop)) {
			String newRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
			rbac.action(new Action("addR", starter, newRole));
			rbac.action(new Action("addHierarchy", starter, newRole, oldTop));
			for(String x : rs.getUA_K().getList()) {
				if(rs.getUA_V(x).contains(oldTop) && !(x.equals(s))) {
					movers.add(x);
					rbac.action(new Action("revokeUser", starter, x, oldTop));
				}
			}
			for(String mover : movers) {
				rbac.action(new Action("assignUser", starter, mover, newRole));
			}
		}
	}

	private void processAdd(GsisBottomState bottom, Rbac1State rs, String o, String g) {
		String top = findTop(g, rs);
		rs.associateRoleToPermission(o, top);
	}

	private void processAddCmd(String starter, String o, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;
		String top = findTop(g, rs);
		rbac.action(new Action("assignPermission", starter, top, o));
	}

	private void processRemove(GsisBottomState bottom, Rbac1State rs, String o, String g) {
		String newRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		rs.addRole(newRole);
		String firstPermRole = findPermOnce(o, g, rs);
		if(firstPermRole != null) {
			HashSet<String> accessors = usersBetweenRoles(findTop(g, rs),
					firstPermRole, rs, new HashSet<String>());
			for(String user : accessors) {
				rs.associateRoleToUser(user, newRole);
			}
			rs.associateRoleToPermission(o, newRole);
			rs.addInheritance(newRole, findBottom(g, rs));
			HashSet<String> permRoles = findPerm(o, g, rs, new HashSet<String>());
			for(String permRole : permRoles) {
				rs.deassociateRoleToPermission(o, permRole);
			}
		}
	}

	private void processRemoveCmd(String starter, String o, String g) {
		Rbac1 rbac = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) rbac.state;

		String newRole = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		rbac.action(new Action("addR", starter, newRole));
		String firstPermRole = findPermOnce(o, g, rs);
		if(firstPermRole != null) {
			HashSet<String> accessors = usersBetweenRoles(findTop(g, rs),
					firstPermRole, rs, new HashSet<String>());
			for(String user : accessors) {
				rbac.action(new Action("assignUser", starter, user, newRole));
			}
			rbac.action(new Action("assignPermission", starter, newRole, o));
			rbac.action(new Action("addHierarchy", starter, newRole, findBottom(g, rs)));
			HashSet<String> permRoles = findPerm(o, g, rs, new HashSet<String>());
			for(String permRole : permRoles) {
				rbac.action(new Action("revokePermission", starter, permRole, o));
			}
		}
	}

	private void initGroup(GsisBottomState bottom, String g, Rbac1State rs) {
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

	private String findPermOnce(String p, String r, Rbac1State rs) {
		if(rs.getPA_K().contains(p) && rs.getPA_V(p).contains(r)) {
			return r;
		}
		for(RolePair roles : rs.getRH().getList()) {
			if(roles.juniorRole.equals(r)) {
				return findPermOnce(p, roles.seniorRole, rs);
			}
		}
		return null;
	}

	private HashSet<String> findPerm(String p, String r, Rbac1State rs, HashSet<String> assignedRoles) {
		if(rs.getPA_K().contains(p) && rs.getPA_V(p).contains(r)) {
			assignedRoles.add(r);
		}
		for(RolePair roles : rs.getRH().getList()) {
			if(roles.juniorRole.equals(r)) {
				return findPerm(p, roles.seniorRole, rs, assignedRoles);
			}
		}
		return assignedRoles;
	}

	private HashSet<String> usersBetweenRoles(String top, String bottom, Rbac1State rs, HashSet<String> users) {
		for(String u : rs.getUA_K().getList()) {
			if(rs.getUA_V(u).contains(top)) {
				users.add(u);
			}
		}
		if(top.equals(bottom)) {
			return users;
		}
		for(RolePair roles : rs.getRH().getList()) {
			if(roles.seniorRole.equals(top)) {
				return usersBetweenRoles(roles.juniorRole, bottom, rs, users);
			}
		}
		return new HashSet<String>();
	}


	@Override
	public void action(Action a) {
		super.action(a);
		if(!GsisBottom.actorToGSIS.containsKey(a.name)) {
			return;
		}
		Rbac1 r = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) scheme.state;
		switch(GsisBottom.actorToGSIS.get(a.name)) {
		case "LAdd":
			processAddCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "LRemove":
			processRemoveCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "addG":
			initGroupCmd(a.params[0], a.params[1]);
			break;
		case "delG":
			HashSet<String> chain = rs.findRoleChain(a.params[1]);
			for(String r2 : chain) {
				r.action(new Action("delR", a.params[0], r2));
			}
			break;
		case "delO":
			r.action(new Action("delP", a.params));
			break;
		case "LLeave":
			processLeaveCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "LJoin":
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
						//exists r3 : Senior(g, r3) and Senior(r2, r3)
						for(RolePair gr : rs.getRH().getList()) {
							if(gr.seniorRole.equals(a.params[3])) {
								String r3 = gr.juniorRole;
								for(RolePair r2r3 : rs.getRH().getList()) {
									if(r2r3.juniorRole.equals(r3) &&
											r2r3.seniorRole.equals(r2)) {

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
					}
				}
			}
			r.lastQueryResult = false;
			break;
		}
		default:
			SimLogger.log(Level.WARNING, getClass().getName() + ": Cannot execute action " + a);
		}

	}

}
