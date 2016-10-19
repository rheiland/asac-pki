package ac;

import java.util.HashSet;
import java.util.Set;

import base.Action;
import base.IAccessAC;
import base.InvalidMeasureException;
import base.Property;
import base.Scheme;
import base.SimLogger.Log;
import base.State;
import base.WorkloadScheme;

/**
 * RBAC with RH.
 * @author yechen
 *
 */
public class Rbac1 extends Scheme {
	public Rbac1(State initState) {
		super(initState);
	}

	@Override
	public boolean isMeasurable(Property p) {
		if(super.isMeasurable(p)) return true;
		Property qTest = new Property(this, "__test", new String[0]);
		if(!qTest.matchQualifier(p)) {
			return false;
		}
		if(p.name.equals(Q_ACTIONS)) {
			return true;
		}
		return false;
	}

	@Override
	public Set<Property> getMeasurables() {
		HashSet<Property> ret = new HashSet<Property>();
		ret.addAll(super.getMeasurables());
		ret.add(new Property(this, Q_ACTIONS, new String[0]));
		return ret;
	}

	@Override
	public Object getCurMeasure(Property p) {
		try {
			if(super.isMeasurable(p)) {
				return super.getCurMeasure(p);
			}
		} catch(InvalidMeasureException e) {
			e.printStackTrace();
		}
		Property qTest = new Property(this, "__test", new String[0]);
		if(!qTest.matchQualifier(p)) {
			throw new InvalidMeasureException(p);
		}
		switch(p.name) {
		case WorkloadScheme.Q_ACTIONS:
			return AVAIL_ACTIONS;
		}
		throw new InvalidMeasureException(p);
	}

	private void command(Action acAction) {
		Rbac1State rState = (Rbac1State) state;
		switch(acAction.name) {
		case "addR":
			rState.addRole(acAction.params[1]);
			break;
		case "delR":
			rState.deleteRole(acAction.params[1]);
			break;
		case "addP":
			rState.addPermission(acAction.params[1]);
			break;
		case "delP":
			rState.delPermission(acAction.params[1]);
			break;
		case "assignUser":
			rState.associateRoleToUser(acAction.params[1], acAction.params[2]);
			break;
		case "revokeUser":
			rState.deassociateRoleToUser(acAction.params[1], acAction.params[2]);
			break;
		case "assignPermission":
			rState.associateRoleToPermission(acAction.params[2], acAction.params[1]);
			break;
		case "revokePermission":
			rState.deassociateRoleToPermission(acAction.params[2], acAction.params[1]);
			break;
		case "addHierarchy":
			rState.addInheritance(acAction.params[1], acAction.params[2]);
			break;
		case "removeHierarchy":
			rState.deleteInheritence(acAction.params[1], acAction.params[2]);
		default:
			Log.e("Unrecognized Command: " + acAction.name);
		}
	}

	public void query(Action acAction) {
		Rbac1State rState = (Rbac1State) state;
		switch(acAction.name) {
		case "UR": {
			IAccessAC<String> UA_K = rState.getUA_K();
			while(UA_K.hasNext()) {
				String u = UA_K.next();
				if(u.equals(acAction.params[1])) {
					IAccessAC<String> UA_V = rState.getUA_V(u);
					while(UA_V.hasNext()) {
						String r = UA_V.next();
						if(r.equals(acAction.params[2])) {
							lastQueryResult = true;
							return;
						}
					}
				}
			}
			lastQueryResult = false;
			return;
		}
		case "PA": {
			//Params: (r, p), which is opposite to my storage.
			IAccessAC<String> PA_K = rState.getPA_K();
			while(PA_K.hasNext()) {
				String p = PA_K.next();
				if(p.equals(acAction.params[2])) {
					IAccessAC<String> PA_V = rState.getPA_V(p);
					while(PA_V.hasNext()) {
						String r = PA_V.next();
						if(r.equals(acAction.params[1])) {
							lastQueryResult = true;
							return;
						}
					}
				}
			}
			lastQueryResult = false;
			return;
		}
		case "RH": {
			IAccessAC<RbacState.RolePair> RH = rState.getRH();
			if(RH.contains(rState.new RolePair(acAction.params[1], acAction.params[2]))) {
				lastQueryResult = true;
				return;
			}
			lastQueryResult = false;
			return;
		}
		case "Senior": {
			Action rhTest = new Action(true, "RH", acAction.params);
			query(rhTest);
			if(lastQueryResult) {
				lastQueryResult = true;
				return;
			}
			IAccessAC<String> R = rState.getR();
			while(R.hasNext()) {
				String r3 = R.next();
				Action senior1Test = new Action(true, "Senior", acAction.params[0], acAction.params[1], r3);
				Action senior2Test = new Action(true, "Senior", acAction.params[0], r3, acAction.params[2]);
				query(senior1Test);
				boolean senior1 = lastQueryResult;
				query(senior2Test);
				boolean senior2 = lastQueryResult;
				if(senior1 && senior2) {
					lastQueryResult = true;
					return;
				}
			}
			lastQueryResult = false;
			return;
		}
		case "auth": {
			IAccessAC<String> R1 = rState.getR();
			while(R1.hasNext()) {
				String r1 = R1.next();
				IAccessAC<String> R2 = rState.getR();
				while(R2.hasNext()) {
					String r2 = R2.next();

					Action urAction = new Action(true, "UR", acAction.params[0], acAction.params[1], r1);
					Action paAction = new Action(true, "PA", acAction.params[0], r2, acAction.params[2]);
					query(urAction);
					boolean urResult = lastQueryResult;
					query(paAction);
					boolean paResult = lastQueryResult;
					if(urResult && paResult) {
						//check whether r1 == r2 or Senior(r1, r2)
						if(r1.equals(r2)) {
							lastQueryResult = true;
							return;
						}

						Action seniorAction = new Action(true, "Senior", acAction.params[0], r1, r2);
						query(seniorAction);
						boolean seniorResult = lastQueryResult;
						if(seniorResult) {
							lastQueryResult = true;
							return;
						}
					}
				}
			}
			lastQueryResult = false;
			return;
		}
		}
	}

	@Override
	public void action(Action acAction) {
		super.action(acAction);
		if(!acAction.isQuery) {
			command(acAction);
		} else {
			query(acAction);
		}

	}


	public static final String[] AVAIL_ACTIONS = {"addR", "delR", "addP", "delP",
	                                              "assignUser", "revokeUser", "assignPermission", "revokePermission", "addHierarchy",
	                                              "removeHierarchy", "UR", "PA", "auth", "Senior", "RH"
	                                             };

}
