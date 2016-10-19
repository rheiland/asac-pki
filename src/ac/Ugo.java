package ac;

import java.util.HashSet;
import java.util.Set;

import base.Scheme;
import base.State;
import base.Action;
import base.IAccessAC;
import base.InvalidMeasureException;
import base.Property;
import base.WorkloadScheme;

public class Ugo extends Scheme {
	public static final String[] AVAIL_ACTIONS = {
		"addS", "delS", "addO", "delO", "addG", "delG", "changeOwner", "changeGroup",
		"grantOwner", "revokeOwner", "grantGroup", "revokeGroup", "grantOther", "revokeOther",
		"addMember", "delMember"
	};

	public Ugo(State initState) {
		super(initState);
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

	private void query(Action acAction) {
		UgoState uState = (UgoState) state;
		switch(acAction.name) {
		case "Member": {
			String u = acAction.params[1];
			String g = acAction.params[2];
			if (uState.getMember_K().contains(u) && 
					uState.getMember_V(u).contains(g)){
				lastQueryResult = true;
				return;
			}
			lastQueryResult = false;
			return;
		}
		case "OwnerAccess": {
			IAccessAC<String> ownerK = uState.getOwner_K();
			if(ownerK.contains(acAction.params[1])) {
				String ownerV = uState.getOwner_V(acAction.params[1]);
				if(ownerV.equals(acAction.params[2])) {
					lastQueryResult = true;
					return;
				}
			}
			lastQueryResult = false;
			return;
		}
		}


	}

	private void command(Action acAction) {
		UgoState uState = (UgoState) state;
		switch(acAction.name) {
		case "addS":
			uState.addS(acAction.params[1]);
			break;
		case "delS":
			uState.delS(acAction.params[1]);
			break;
		case "addO":
			uState.addO(acAction.params[1]);
			break;
		case "delO":
			uState.delO(acAction.params[1]);
			break;
		case "addG":
			uState.addG(acAction.params[1]);
			break;
		case "delG":
			uState.delG(acAction.params[1]);
			break;
		case "changeOwner":
			uState.changeOwner(acAction.params[1], acAction.params[2]);
			break;
		case "changeGroup":
			uState.changeGroup(acAction.params[1], acAction.params[2]);
			break;
		case "grantOwner":
			uState.grantOwner(acAction.params[1], acAction.params[2]);
			break;
		case "revokeOwner":
			uState.revokeOwner(acAction.params[1], acAction.params[2]);
			break;
		case "grantGroup":
			uState.grantGroup(acAction.params[1], acAction.params[2]);
			break;
		case "revokeGroup":
			uState.revokeGroup(acAction.params[1], acAction.params[2]);
			break;
		case "grantOther":
			uState.grantOther(acAction.params[1], acAction.params[2]);
			break;
		case "revokeOther":
			uState.revokeOther(acAction.params[1], acAction.params[2]);
			break;
		case "addMember":
			uState.addMember(acAction.params[1], acAction.params[2]);
			break;
		case "delMember":
			uState.delMember(acAction.params[1], acAction.params[2]);
			break;
		}

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

}
