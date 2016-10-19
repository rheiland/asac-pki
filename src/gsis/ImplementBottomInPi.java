package gsis;

import base.State;
import base.Action;
import base.Implementation;
import base.WorkloadState;

public class ImplementBottomInPi extends Implementation {

	@Override
	public void init(WorkloadState ws) {
		wScheme = new GsisBottom(ws);
		scheme = new ac.GsisPi(stateMap(ws));
	}

	public static String schemeName() {
		return "pi";
	}

	@Override
	public State stateMap(WorkloadState ws) {
		GsisBottomState rs = (GsisBottomState) ws;
		ac.GsisPiState ps = new ac.GsisPiState();

		for(String s : rs.getSOld().getList()) {
			ps.addSubject(s);
		}

		for(String o : rs.getOOld().getList()) {
			ps.addObject(o);
		}

		for(String g : rs.getGOld().getList()) {
			ps.addGroup(g);
		}

		for(SGPair sg : rs.getLiberalLeaves_K().getList()) {
			for(Integer t : rs.getLiberalLeaves_V(sg).getList()) {
				ps.setTimeStamp(t);
				ps.addLiberalLeave(sg.subject, sg.group);
			}
		}

		for(OGPair og : rs.getLiberalRemove_K().getList()) {
			for(Integer t: rs.getLiberalRemove_V(og).getList()) {
				ps.setTimeStamp(t);
				ps.addLiberalRemove(og.object, og.group);
			}
		}

		for(OGPair og : rs.getLiberalAdd_K().getList()) {
			for(Integer t : rs.getLiberalAdd_V(og).getList()) {
				ps.setTimeStamp(t);
				ps.addLiberalAdd(og.object, og.group);
			}
		}

		for(SGPair sg : rs.getLiberalJoins_K().getList()) {
			for(Integer t : rs.getLiberalJoins_V(sg).getList()) {
				ps.setTimeStamp(t);
				ps.addLiberalJoin(sg.subject, sg.group);
			}
		}

		ps.setTimeStamp(rs.getTimestamp());
		return ps;
	}

	@Override
	public void action(Action a) {
		super.action(a);
		scheme.action(a);
	}

}
