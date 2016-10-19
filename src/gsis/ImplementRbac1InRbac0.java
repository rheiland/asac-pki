package gsis;

import java.util.HashSet;

import gsis.RbacState.RolePair;
import base.State;
import base.Action;
import base.Implementation;
import base.WorkloadState;
import ac.Rbac0State;
import ac.Rbac0;

public class ImplementRbac1InRbac0 extends Implementation {

	@Override
	public State stateMap(WorkloadState ws) {
		//workload state: with role hierarchy
		Rbac1State rhs = (Rbac1State) ws;
		//ac: without role hierarchy
		Rbac0State rs = new Rbac0State();

		for(String u : rhs.getU().getList()) {
			rs.addUser(u);
		}

		for(String r : rhs.getR().getList()) {
			rs.addRole(r);
		}

		for(String p : rhs.getP().getList()) {
			rs.addPermission(p);
		}

		for(RolePair rp : rhs.getRH().getList()) {
			processLink(rp.seniorRole, rp.juniorRole, rs);
		}

		buildUA(rhs, rs);
		buildPA(rhs, rs);
		return rs;
	}

	public static String schemeName() {
		return "Rbac0";
	}

	private void processLink(String senior, String junior, Rbac0State rs) {
		HashSet<String> prefixes = new HashSet<String>();
		HashSet<String> suffixes = new HashSet<String>();

		for(String role : rs.getR().getList()) {
			if(role.startsWith(junior + sentinel()) || role.equals(junior)) {
				suffixes.add(role);
			} else if(role.endsWith(sentinel() + senior) || role.equals(senior)) {
				prefixes.add(role);
			}
		}

		for(String prefix : prefixes) {
			for(String suffix : suffixes) {
				rs.addRole(prefix + sentinel() + suffix);
			}
		}
	}

	private void buildUA(Rbac1State rhs, Rbac0State rs) {
		for(String u : rhs.getUA_K().getList()) {
			for(String r : rhs.getUA_V(u).getList()) {
				rs.associateRoleToUser(u, r);
				for(String q : rs.getR().getList()) {
					if(q.startsWith(r + sentinel())) {
						rs.associateRoleToUser(u, q);
					}
				}
			}
		}
	}

	private void buildPA(Rbac1State rhs, Rbac0State rs) {
		for(String p : rhs.getPA_K().getList()) {
			for(String r: rhs.getPA_V(p).getList()) {
				rs.associateRoleToPermission(p, r);
				for(String q : rs.getR().getList()) {
					if(q.endsWith(sentinel() + r)) {
						rs.associateRoleToPermission(p, q);
					}
				}
			}
		}
	}


	@Override
	public void action(Action a) {
		Rbac0 r = (Rbac0) scheme;
		Rbac0State rs = (Rbac0State) r.state;
		super.action(a);
		switch(a.name) {
		case "addR":
			r.action(a);
			break;
		case "delR":
			r.action(a);
			for(String q : rs.getR().getList()) {
				Action delq = new Action(a.name, a.params[0], q);
				if(q.contains(sentinel() + a.params[1] + sentinel())) {
					r.action(delq);
				} else if(q.startsWith(a.params[1] + sentinel())) {
					r.action(delq);
				} else if(q.endsWith(sentinel() + a.params[1])) {
					r.action(delq);
				}
			}
			break;
		case "addP":
			r.action(a);
			break;
		case "delP":
			r.action(a);
			break;
		case "assignUser":
			r.action(a);
			for(String q : rs.getR().getList()) {
				Action assignq = new Action(a.name, a.params[0], a.params[1], q);
				if(q.startsWith(a.params[2] + sentinel())) {
					r.action(assignq);
				}
			}
			break;
		case "revokeUser":
			r.action(a);
			for(String q : rs.getR().getList()) {
				if(q.startsWith(a.params[2] + sentinel())) {
					r.action(new Action(a.name, a.params[0], a.params[1], q));
				}
			}
			break;
		case "assignPermission":
			r.action(a);
			for(String q : rs.getR().getList()) {
				if(q.endsWith(sentinel() + a.params[1])) {
					r.action(new Action(a.name, a.params[0], q, a.params[2]));
				}
			}
			break;
		case "revokePermission":
			r.action(a);
			for(String q : rs.getR().getList()) {
				if(q.endsWith(sentinel() + a.params[1])) {
					r.action(new Action(a.name, a.params[0], q, a.params[2]));
				}
			}
			break;
		case "addHierarchy": {
			HashSet<String> prefixes = new HashSet<String>();
			HashSet<String> suffixes = new HashSet<String>();
			String s = a.params[1];
			String j = a.params[2];
			for(String q : rs.getR().getList()) {
				if(q.startsWith(j + sentinel()) || q.equals(j)) {
					suffixes.add(q);
				} else if(q.endsWith(sentinel() + s) || q.equals(s)) {
					prefixes.add(q);
				}
			}

			for(String prefix : prefixes) {
				for(String suffix : suffixes) {
					String chain = prefix + sentinel() + suffix;
					r.action(new Action("addR", a.params[0], chain));
					for(String u : rs.getUA_K().getList()) {
						for(String q : rs.getUA_V(u).getList()) {
							if(chain.startsWith(q + sentinel())) {
								r.action(new Action("assignUser", a.params[0],
								                    u, chain));
							}
						}
					}
					for(String p : rs.getPA_K().getList()) {
						for(String q : rs.getPA_V(p).getList()) {
							if(chain.endsWith(sentinel() + q)) {
								r.action(new Action("assignPermission", a.params[0],
								                    chain, p));
							}
						}
					}
				}
			}
			break;
		}
		case "removeHierarchy": {
			String s = a.params[1];
			String j = a.params[2];
			for(String q : rs.getR().getList()) {
				if(q.contains(s + sentinel() + j)) {
					r.action(new Action("delR", a.params[0], q));
				}
			}
		}
		break;
		}
	}

	@Override
	public void init(WorkloadState ws) {
		wScheme = new Rbac1(ws);
		scheme = new Rbac0(stateMap(ws));

	}

}
