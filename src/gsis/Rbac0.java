package gsis;

import java.util.HashSet;
import java.util.Set;

import base.Action;
import base.IAccessW;
import base.InvalidMeasureException;
import base.Property;
import base.SimLogger.Log;
import base.Workflow;
import base.WorkloadScheme;
import base.WorkloadState;

/**
 * RBAC without RH.
 * @author yechen
 *
 */
public class Rbac0 extends WorkloadScheme {

	public Rbac0(WorkloadState initState) {
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
		Rbac0State rState = (Rbac0State) state;
		switch(acAction.name) {
		case "addR":
			rState.addRole(acAction.params[1]);
			break;
		case "delR":
			rState.delRole(acAction.params[1]);
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
		default:
			Log.e("Unrecognized Command: " + acAction.name);
		}
	}

	private void query(Action acAction) {
		Rbac0State rState = (Rbac0State) state;
		switch(acAction.name) {
		case "UR": {
			IAccessW<String> UA_K = rState.getUA_K();
			while(UA_K.hasNext()) {
				String u = UA_K.next();
				if(u.equals(acAction.params[1])) {
					IAccessW<String> UA_V = rState.getUA_V(u);
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
			IAccessW<String> PA_K = rState.getPA_K();
			while(PA_K.hasNext()) {
				String p = PA_K.next();
				if(p.equals(acAction.params[2])) {
					IAccessW<String> PA_V = rState.getPA_V(p);
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
		case "auth": {
			IAccessW<String> R = rState.getR();
			while(R.hasNext()) {
				String r = R.next();
				Action urAction = new Action(true, "UR", acAction.params[0], acAction.params[1], r);
				Action paAction = new Action(true, "PA", acAction.params[0], r, acAction.params[2]);
				query(urAction);
				boolean urResult = lastQueryResult;
				query(paAction);
				boolean paResult = lastQueryResult;
				if(urResult && paResult) {
					lastQueryResult = true;
					return;
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
	                                              "assignUser", "revokeUser", "assignPermission", "revokePermission", "UR", "PA", "auth", "Senior", "RH"
	                                             };


	@Override
	public Action addParameters(Action a, Workflow w) {
		// TODO Auto-generated method stub
		//This is not needed for this simulation. We're only using this to indirectly simulate between GSIS and UGO.
		return null;
	}
}
