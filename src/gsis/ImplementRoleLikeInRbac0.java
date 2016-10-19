package gsis;

import java.util.logging.Level;

import ac.Rbac0;
import ac.Rbac0State;
import base.State;
import base.Action;
import base.IAccessW;
import base.Implementation;
import base.SimLogger;
import base.WorkloadState;

/**
 * GSIS to plain RBAC (with role hierarchy, same without)
 * @author yechen
 *
 */
public class ImplementRoleLikeInRbac0 extends Implementation {

	@Override
	public void init(WorkloadState ws) {
		wScheme = new GsisRoleLike(ws);
		scheme = new Rbac0(stateMap(ws));
	}

	public static String schemeName() {
		return "Rbac0";
	}

	@Override
	public State stateMap(WorkloadState ws) {
		GsisRoleLikeState s = (GsisRoleLikeState) ws;
		Rbac0State rs = new Rbac0State();
		IAccessW<String> gA = s.getG();
		IAccessW<String> sA = s.getS();
		IAccessW<String> oA = s.getO();
		while(gA.hasNext()) {
			String g = gA.next();
			rs.addRole(g);
		}
		while(sA.hasNext()) {
			String u = sA.next();
			rs.addUser(u);
		}
		while(oA.hasNext()) {
			String p = oA.next();
			rs.addPermission(p);
		}

		IAccessW<SGPair> joinA = s.getLiberalJoins_K();

		//In Join after leave = membership
		while(joinA.hasNext()) {
			SGPair pair = joinA.next();
			IAccessW<Integer> joinA_V = s.getLiberalJoins_V(pair);
			while(joinA_V.hasNext()) {
				Integer jointime = joinA_V.next(); //joined
				IAccessW<Integer> leaveA_V = s.getStrictLeaves_V(pair);
				boolean joined = true;
				while(leaveA_V.hasNext()) {
					Integer leavetime = leaveA_V.next(); //left
					if(leavetime > jointime) {
						joined = false; //left after joined = join is useless now.
						break;
					}
				}
				if(joined) {
					rs.associateRoleToUser(pair.subject, pair.group);
					break;
				}
			}
		}

		IAccessW<OGPair> addA = s.getLiberalAdd_K();
		//Add after remove = role has permission
		while(addA.hasNext()) {
			OGPair pair = addA.next();
			IAccessW<Integer> addA_V = s.getLiberalAdd_V(pair);
			while(addA_V.hasNext()) {
				Integer addTime = addA_V.next(); //added
				IAccessW<Integer> removeA_V = s.getStrictRemove_V(pair);
				boolean added = true;
				while(removeA_V.hasNext()) {
					Integer removeTime = removeA_V.next(); //removed
					if(removeTime > addTime) {
						//really removed
						added = false;
						break;
					}
				}
				if(added) {
					rs.associateRoleToPermission(pair.object, pair.group);
					break;
				}
			}
		}

		//RH is empty.
		return rs;
	}

	@Override
	public void action(Action a) {
		super.action(a);
		if(!GsisRoleLike.actorToGSIS.containsKey(a.name)) {
			return;
		}
		Rbac0 r = (Rbac0) scheme;
		Rbac0State rs = (Rbac0State) scheme.state;
		switch(GsisRoleLike.actorToGSIS.get(a.name)) {
		case "delO":
			r.action(new Action("delP", a.params));
			break;
		case "delG":
			r.action(new Action("delR", a.params));
			break;
		case "addO":
			r.action(new Action("addP", a.params));
			break;
		case "LAdd":
			//object, group
			r.action(new Action("assignPermission", a.params[0], a.params[2], a.params[1]));
			break;
		case "SRemove":
			r.action(new Action("revokePermission", a.params[0], a.params[2], a.params[1]));
			break;
		case "addG":
			r.action(new Action("addR", a.params));
			break;
		case "SLeave":
			//subject, group
			r.action(new Action("revokeUser", a.params));
			break;
		case "LJoin":
			//subject, group
			r.action(new Action("assignUser", a.params));
			break;
		case "auth":
			//s,o,g
		{
			if(rs.getUA_K().contains(a.params[1]) &&
			                rs.getUA_V(a.params[1]).contains(a.params[3])) {
				if(rs.getPA_K().contains(a.params[2]) &&
				                rs.getPA_V(a.params[3]).contains(a.params[3])) {
					r.lastQueryResult = true;
				}
			}
			r.lastQueryResult = false;
		}
		break;
		default:
			SimLogger.log(Level.WARNING, getClass().getName() + ": Cannot execute action " + a);
		}
	}

}
