package gsis;

import java.util.HashSet;

import ac.Ugo;
import ac.UgoState;
import ac.SGPair;
import base.State;
import base.Action;
import base.Implementation;
import base.Simulation;
import base.WorkloadState;

public class ImplementRbac0InUgo extends Implementation {

	@Override
	public void init(WorkloadState ws) {
		wScheme = new Rbac0(ws);
		scheme = new ac.Ugo(stateMap(ws));
	}

	public static String schemeName() {
		return "ugo";
	}

	@Override
	public State stateMap(WorkloadState ws) {
		Rbac0State rs = (Rbac0State) ws;
		UgoState us = new UgoState();

		for(String u : rs.getU().getList()) {
			us.addS(u);
		}

		for(String x : rs.getR().getList()) {
			us.addS(x);
		}

		for(String p : rs.getP().getList()) {
			us.addO(p);
		}

		for(String r : rs.getR().getList()) {
			us.addG(r);
		}

		for(String x : rs.getP().getList()) {
			us.addG(x);
		}

		for(String x : rs.getUA_K().getList()) {
			for(String y : rs.getUA_V(x).getList()) {
				us.addMember(x, y);
			}
		}

		for(String y : rs.getPA_K().getList()) {
			for(String x : rs.getPA_V(y).getList()) {
				us.addMember(x, y);
			}
		}

		for(String o : us.getO().getList()) {
			String grp = "g" + us.getG().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
			us.addG(grp);
			us.changeGroup(o, grp);
			us.grantGroup(o, "read");
			for (String s : us.getMember_K().getList()){
				for (String r : us.getMember_V(s).getList()){
					//r in S, r in G, (r, o) in Member
					if (us.getS().contains(r) && us.getG().contains(r) &&
							us.getMember_K().contains(r) && us.getMember_V(r).contains(o)){
						us.addMember(s, grp);
					}
				}
			}
		}


		return us;
	}

	@Override
	public void action(Action a) {
		Ugo ugo = (Ugo) scheme;
		UgoState ugoS = (UgoState) ugo.state;
		super.action(a);
		switch(a.name) {
		case "addU":
			ugo.action(new Action("addS", a.params));
			break;
		case "delU":
			if(!ugoS.getG().contains(a.params[1])) {
				ugo.action(new Action("delS", a.params));
			}
			break;
		case "addR":
			ugo.action(new Action("addS", a.params));
			ugo.action(new Action("addG", a.params));
			break;
		case "delR":
			if(ugoS.getS().contains(a.params[1]) && ugoS.getG().contains(a.params[1])) {
				ugo.action(new Action("delS", a.params));
				ugo.action(new Action("delG", a.params));
			}
			break;
		case "addP": {
			ugo.action(new Action("addO", a.params));
			ugo.action(new Action("addG", a.params));
			String grp = "g" + ugoS.getG().size() + Simulation.rand.nextLong() + Simulation.rand.nextLong();
			ugo.action(new Action("addG", a.params[0], grp));
			ugo.action(new Action("changeGroup", a.params[0], a.params[1], grp));
			ugo.action(new Action("grantGroup", a.params[0], a.params[1], "read"));
			break;
		}
		case "delP": {
			HashSet<String> batchDel = new HashSet<String>();
			for(String s : ugoS.getS().getList()) {
				batchDel.add(s);
			}
			for(String g : ugoS.getG().getList()) {
				batchDel.add(g);
			}
			for(String o : ugoS.getO().getList()) {
				batchDel.add(o);
			}

			for(String g: batchDel) {
				boolean test = true;
				for(String o : ugoS.getGroup_K().getList()) {
					if(ugoS.getGroup_V(o).contains(g)) {
						test = false;
						break;
					}
				}
				if(test) {
					ugo.action(new Action("delG", a.params[0], g));
				}
			}

			ugo.action(new Action("delO", a.params));
			ugo.action(new Action("delG", a.params));
		}
		break;
		case "assignUser": {
			ugo.action(new Action("addMember", a.params));
			String r = a.params[2];
			if (ugoS.getMember_K().contains(r)){
				for (String o : ugoS.getMember_V(r).getList()){
					if (ugoS.getO().contains(o)){
						if (ugoS.getGroup_K().contains(o)){
							String g = ugoS.getGroup_V(o);
							ugo.action(new Action("addMember", a.params[0], a.params[1], g));
						}
					}
				}
			}
			break;
		}
		case "revokeUser": {
			if(!ugoS.getS().contains(a.params[2]) || !ugoS.getG().contains(a.params[2])) {
				return;
			}
			ugo.action(new Action("delMember", a.params));
			ugoS = (UgoState) ugo.state;
			String r = a.params[2];
			if (ugoS.getMember_K().contains(r)){
				for (String o : ugoS.getMember_V(r).getList()){
					if (ugoS.getO().contains(o)){
						if (ugoS.getGroup_K().contains(o)){
							String g = ugoS.getGroup_V(o);
							ugo.action(new Action("delMember", a.params[0], a.params[1], g));
						}
					}
				}
			}
			break;
		}
		case "assignPermission": {
			String r = a.params[1];
			String p = a.params[2];
			ugo.action(new Action("addMember", a.params));
			ugoS = (UgoState) ugo.state;
			for (String u : ugoS.getMember_K().getList()){
				if (!ugoS.getG().contains(u) && ugoS.getMember_V(u).contains(r)){
					if(ugoS.getGroup_K().contains(p)) {
						String g = ugoS.getGroup_V(p);
						ugo.action(new Action("addMember", a.params[0], u, g));
					}
				}
			}
			break;
		}
		case "revokePermission": {
			String r = a.params[1];
			String p = a.params[2];
			if(!ugoS.getO().contains(p)) {
				return;
			}
			ugo.action(new Action("delMember", a.params));
			ugoS = (UgoState) ugo.state;
			for (String u : ugoS.getMember_K().getList()){
				if (!ugoS.getG().contains(u) && ugoS.getMember_V(u).contains(r)){
					if(ugoS.getGroup_K().contains(p)) {
						String g = ugoS.getGroup_V(p);
						ugo.action(new Action("delMember", a.params[0], u, g));
					}
				}
			}
			break;
		}

		}
	}

}
