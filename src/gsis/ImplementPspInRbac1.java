package gsis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import base.State;
import base.Action;
import base.Implementation;
import base.Simulation;
import base.WorkloadState;
import ac.RbacState.RolePair;
import ac.Rbac1State;
import ac.Rbac1;

public class ImplementPspInRbac1 extends Implementation {

	@Override
	public void init(WorkloadState ws) {
		wScheme = new GsisPsp(ws);
		scheme = new ac.Rbac1(stateMap(ws));
	}

	public static String schemeName() {
		return "Rbac1";
	}

	@Override
	public State stateMap(WorkloadState ws) {
		Rbac1State rs = new Rbac1State();
		GsisPspState gs = (GsisPspState) ws;
		for(String s : gs.getS().getList()) {
			rs.addUser(s);
		}
		for(String g : gs.getG().getList()) {
			initGroup(g, rs);
		}
		for(String o : gs.getO().getList()) {
			rs.addPermission(o);
		}

		HashMap<Integer, Object> records = new HashMap<Integer, Object>();
		List<Integer> timeorder = new ArrayList<Integer>();

		for(SGPair sg : gs.getLiberalJoins_K().getList()) {
			for(Integer t : gs.getLiberalJoins_V(sg).getList()) {
				records.put(t, sg);
				timeorder.add(t);
			}
		}

		for(SGPair sg : gs.getStrictLeaves_K().getList()) {
			for(Integer t : gs.getStrictLeaves_V(sg).getList()) {
				records.put(t, sg);
				timeorder.add(t);
			}
		}

		for(OGPair og : gs.getLiberalAdd_K().getList()) {
			for(Integer t : gs.getLiberalAdd_V(og).getList()) {
				records.put(t, og);
				timeorder.add(t);
			}
		}

		for(OGPair og : gs.getLiberalRemove_K().getList()) {
			for(Integer t : gs.getLiberalRemove_V(og).getList()) {
				records.put(t, og);
				timeorder.add(t);
			}
		}

		for(OGPair og : gs.getStrictRemove_K().getList()) {
			for(Integer t : gs.getStrictRemove_V(og).getList()) {
				records.put(t, og);
				timeorder.add(t);
			}
		}

		Collections.sort(timeorder);

		for(Integer t : timeorder) {
			Object o = records.get(t);
			if(o instanceof SGPair) {
				SGPair sg = (SGPair) o;
				//join or leave
				if(gs.getLiberalJoins_K().contains(sg)) {
					processLJoin(sg.subject, sg.group, rs);
				} else if(gs.getStrictLeaves_K().contains(sg)) {
					processSLeave(sg.subject, sg.group, rs);
				}
			} else if(o instanceof OGPair) {
				OGPair og = (OGPair) o;
				//add or remove
				if(gs.getLiberalAdd_K().contains(og)) {
					processLAdd(og.object, og.group, rs);
				} else if(gs.getLiberalRemove_K().contains(og)) {
					processLRemove(og.object, og.group, rs);
				} else if(gs.getStrictRemove_K().contains(og)) {
					processSRemove(og.object, og.group, rs);
				}
			} else {
				base.SimLogger.log(Level.SEVERE, "An object in records is neither SGPair nor OGPair");
				throw new RuntimeException();
			}
		}

		return rs;
	}

	private void initGroup(String g, Rbac1State rs) {
		rs.addRole(g);
		String subA = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		rs.addRole(subA);
		String subB = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		rs.addRole(subB);
		rs.addInheritance(g, subA);
		rs.addInheritance(subA, subB);
	}

	private void initGroupCmd(String starter, String g) {
		Rbac1 r = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) r.state;

		r.action(new Action("addR", starter, g));
		String subA = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		r.action(new Action("addR", starter, subA));
		String subB = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
		r.action(new Action("addR", starter, subA));
		r.action(new Action("addHierarchy", starter, g, subA));
		r.action(new Action("addHierarchy", starter, subA, subB));
	}

	private void processLJoin(String s, String g, Rbac1State rs) {
		rs.associateRoleToUser(s, g);
		if(rs.getUA_K().contains(s)) {
			for(RolePair gx : rs.getRH().getList()) {
				if(gx.seniorRole.equals(g)) {
					String x = gx.juniorRole;
					for(RolePair yx : rs.getRH().getList()) {
						if(yx.juniorRole.equals(x)) {
							String y = yx.seniorRole;
							if(rs.getUA_V(s).contains(y)) {
								for(RolePair zy : rs.getRH().getList()) {
									if(zy.juniorRole.equals(y)) {
										String z = zy.seniorRole;
										rs.associateRoleToUser(s, z);
										rs.deassociateRoleToUser(s, y);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void processLJoinCmd(String starter, String s, String g) {
		Rbac1 r = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) r.state;
		r.action(new Action("assignUser", starter, s, g));
		if(rs.getUA_K().contains(s)) {
			for(RolePair gx : rs.getRH().getList()) {
				if(gx.seniorRole.equals(g)) {
					String x = gx.juniorRole;
					for(RolePair yx : rs.getRH().getList()) {
						if(yx.juniorRole.equals(x)) {
							String y = yx.seniorRole;
							if(rs.getUA_V(s).contains(y)) {
								for(RolePair zy : rs.getRH().getList()) {
									if(zy.juniorRole.equals(y)) {
										String z = zy.seniorRole;
										r.action(new Action("assignUser", starter, s, z));
										r.action(new Action("revokeUser", starter, s, y));
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void processSLeave(String s, String g, Rbac1State rs) {
		if(!rs.getUA_K().contains(s) || !rs.getUA_V(s).contains(g)) {
			return;
		}
		String sup1 = null, sup2 = null;
		//has x, y, z: (g, x), (y, x), (z, y) in RH && (s, z) in UR
		//Implicit: UR has key s
		boolean hasBuilt = false;
		for(RolePair gx : rs.getRH().getList()) {
			if(gx.seniorRole.equals(g)) {
				String x = gx.juniorRole;
				for(RolePair yx : rs.getRH().getList()) {
					if(yx.juniorRole.equals(x)) {
						String y = yx.seniorRole;
						for(RolePair zy : rs.getRH().getList()) {
							if(zy.juniorRole.equals(y)) {
								String z = zy.seniorRole;
								if(rs.getUA_V(s).contains(z)) {
									sup1 = y;
									sup2 = z;
									rs.deassociateRoleToUser(s, sup2);
									rs.associateRoleToUser(s, sup1);
									hasBuilt = true;
								}
							}
						}
					}
				}
			}
		}

		if(!hasBuilt) {
			//Never built the hierarchy for this user before below g, do it now.
			sup1 = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
			rs.addRole(sup1);
			sup2 = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
			rs.addRole(sup2);
			rs.addInheritance(sup2, sup1);
			for(RolePair gx : rs.getRH().getList()) {
				if(gx.seniorRole.equals(g)) {
					String x = gx.juniorRole;
					rs.addInheritance(sup1, x);
				}
			}
			rs.associateRoleToUser(s, sup1);
		}
		for(String p : rs.getPA_K().getList()) {
			if(rs.getPA_V(p).contains(g)) {
				rs.associateRoleToPermission(p, sup2);
			}
		}
		rs.deassociateRoleToUser(s, g);
	}

	private void processSLeaveCmd(String starter, String s, String g) {
		Rbac1 r = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) r.state;

		if(!rs.getUA_K().contains(s) || !rs.getUA_V(s).contains(g)) {
			return;
		}
		String sup1 = null, sup2 = null;
		//has x, y, z: (g, x), (y, x), (z, y) in RH && (s, z) in UR
		//Implicit: UR has key s
		boolean hasBuilt = false;
		for(RolePair gx : rs.getRH().getList()) {
			if(gx.seniorRole.equals(g)) {
				String x = gx.juniorRole;
				for(RolePair yx : rs.getRH().getList()) {
					if(yx.juniorRole.equals(x)) {
						String y = yx.seniorRole;
						for(RolePair zy : rs.getRH().getList()) {
							if(zy.juniorRole.equals(y)) {
								String z = zy.seniorRole;
								if(rs.getUA_V(s).contains(z)) {
									sup1 = y;
									sup2 = z;
									r.action(new Action("revokeUser", starter, s, sup2));
									r.action(new Action("assignUser", starter, s, sup1));
									hasBuilt = true;
								}
							}
						}
					}
				}
			}
		}

		if(!hasBuilt) {
			//Never built the hierarchy for this user before below g, do it now.
			sup1 = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
			r.action(new Action("addR", starter, sup1));
			sup2 = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
			r.action(new Action("addR", starter, sup2));
			r.action(new Action("addHierarchy", starter, sup2, sup1));
			for(RolePair gx : rs.getRH().getList()) {
				if(gx.seniorRole.equals(g)) {
					String x = gx.juniorRole;
					r.action(new Action("addHierarchy", starter, sup1, x));
				}
			}
			r.action(new Action("assignUser", starter, s, sup1));
		}
		for(String p : rs.getPA_K().getList()) {
			if(rs.getPA_V(p).contains(g)) {
				r.action(new Action("assignPermission", starter, sup2, p));
			}
		}
		r.action(new Action("revokeUser", starter, s, g));
	}

	private void processLAdd(String o, String g, Rbac1State rs) {
		rs.associateRoleToPermission(o, g);
	}

	private void processLAddCmd(String starter, String o, String g) {
		Rbac1 r = (Rbac1) scheme;

		r.action(new Action("assignPermission", starter, g, o));
	}

	private void processSRemove(String o, String g, Rbac1State rs) {
		rs.deassociateRoleToPermission(o, g);
		for(RolePair ga : rs.getRH().getList()) {
			if(ga.seniorRole.equals(g)) {
				String a = ga.juniorRole;
				for(RolePair ab : rs.getRH().getList()) {
					if(ab.seniorRole.equals(a)) {
						String b = ab.juniorRole;
						//for each sup2: sup1 >= supA, sup2 >= sup1
						for(RolePair s1a: rs.getRH().getList()) {
							if(s1a.juniorRole.equals(a)) {
								String sup1 = s1a.seniorRole;
								for(RolePair s21: rs.getRH().getList()) {
									if(s21.juniorRole.equals(sup1)) {
										String sup2 = s21.seniorRole;
										rs.deassociateRoleToPermission(o, sup2);
									}
								}
							}
						}
						//for each orphan: orphan >= b, orphan != a
						for(RolePair orphanb : rs.getRH().getList()) {
							String orphan = orphanb.seniorRole;
							if(!orphan.equals(a) && orphanb.juniorRole.equals(b)) {
								rs.deassociateRoleToPermission(o, orphan);
							}
						}
					}
				}
			}
		}
	}

	private void processSRemoveCmd(String starter, String o, String g) {
		Rbac1 r = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) r.state;
		r.action(new Action("revokePermission", starter, g, o));

		for(RolePair ga : rs.getRH().getList()) {
			if(ga.seniorRole.equals(g)) {
				String a = ga.juniorRole;
				for(RolePair ab : rs.getRH().getList()) {
					if(ab.seniorRole.equals(a)) {
						String b = ab.juniorRole;
						//for each sup2: sup1 >= supA, sup2 >= sup1
						for(RolePair s1a: rs.getRH().getList()) {
							if(s1a.juniorRole.equals(a)) {
								String sup1 = s1a.seniorRole;
								for(RolePair s21: rs.getRH().getList()) {
									if(s21.juniorRole.equals(sup1)) {
										String sup2 = s21.seniorRole;
										r.action(new Action("revokePermission", starter, sup2, o));
									}
								}
							}
						}
						//for each orphan: orphan >= b, orphan != a
						for(RolePair orphanb : rs.getRH().getList()) {
							String orphan = orphanb.seniorRole;
							if(!orphan.equals(a) && orphanb.juniorRole.equals(b)) {
								r.action(new Action("revokePermission", starter, orphan, o));
							}
						}
					}
				}
			}
		}
	}

	private void processLRemove(String o, String g, Rbac1State rs) {
		if(!rs.getPA_K().contains(o) || !rs.getPA_V(o).contains(g)) {
			return;
		}
		//g >= subA, subA >= subB
		for(RolePair ga : rs.getRH().getList()) {
			if(ga.seniorRole.equals(g)) {
				String a = ga.juniorRole;
				for(RolePair ab : rs.getRH().getList()) {
					if(ab.seniorRole.equals(a)) {
						String b = ab.juniorRole;
						String orphan = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
						rs.addRole(orphan);
						rs.addInheritance(orphan, b);
						for(String u : rs.getUA_K().getList()) {
							if(rs.getUA_V(u).contains(g)) {
								rs.associateRoleToUser(u, orphan);
							}
						}
						rs.associateRoleToPermission(o, orphan);
						rs.deassociateRoleToPermission(o, g);
					}
				}
			}
		}
	}

	private void processLRemoveCmd(String starter, String o, String g) {
		Rbac1 r = (Rbac1) scheme;
		Rbac1State rs = (Rbac1State) r.state;

		if(!rs.getPA_K().contains(o) || !rs.getPA_V(o).contains(g)) {
			return;
		}
		//g >= subA, subA >= subB
		for(RolePair ga : rs.getRH().getList()) {
			if(ga.seniorRole.equals(g)) {
				String a = ga.juniorRole;
				for(RolePair ab : rs.getRH().getList()) {
					if(ab.seniorRole.equals(a)) {
						String b = ab.juniorRole;
						String orphan = "r" + rs.getR().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
						r.action(new Action("addR", starter, orphan));
						r.action(new Action("addHierarchy", starter, orphan, b));

						for(String u : rs.getUA_K().getList()) {
							if(rs.getUA_V(u).contains(g)) {
								r.action(new Action("assignUser", starter, u, orphan));
							}
						}
						r.action(new Action("assignPermission", starter, orphan, o));
						r.action(new Action("revokePermission", starter, g, o));
					}
				}
			}
		}
	}


	@Override
	public void action(Action a) {
		super.action(a);
		if(!GsisPsp.actorToGSIS.containsKey(a.name)) {
			return;
		}
		Rbac1 r = (Rbac1) scheme;
		//Rbac1State rs = (Rbac1State) scheme.state;
		switch(GsisPsp.actorToGSIS.get(a.name)) {
		case "delO":
			r.action(new Action("delP", a.params));
			break;
		case "addO":
			r.action(new Action("addP", a.params));
			break;
		case "LAdd":
			processLAddCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "LRemove":
			processLRemoveCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "LJoin":
			processLJoinCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "SRemove":
			processSRemoveCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "SLeave":
			processSLeaveCmd(a.params[0], a.params[1], a.params[2]);
			break;
		case "auth":
			//TODO
			break;
		}
	}

}
