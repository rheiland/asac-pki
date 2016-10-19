package gsis;

import base.State;
import base.Action;
import base.Implementation;
import base.WorkloadState;

public class ImplementTopInPi extends Implementation {

	@Override
	public void init(WorkloadState ws) {
		wScheme = new GsisTop(ws);
		scheme = new ac.GsisPi(stateMap(ws));
	}

	public static String schemeName() {
		return "pi";
	}

	@Override
	public State stateMap(WorkloadState ws) {
		GsisTopState rs = (GsisTopState) ws;
		ac.GsisPiState ps = new ac.GsisPiState();

		for(String s : rs.getS().getList()) {
			ps.addSubject(s);
		}

		for(String o : rs.getO().getList()) {
			ps.addObject(o);
		}

		for(String g : rs.getG().getList()) {
			ps.addGroup(g);
		}

		for(SGPair sg : rs.getStrictLeaves_K().getList()) {
			for(Integer t : rs.getStrictLeaves_V(sg).getList()) {
				ps.setTimeStamp(t);
				ps.addStrictLeave(sg.subject, sg.group);
			}
		}

		for(OGPair og : rs.getStrictRemove_K().getList()) {
			for(Integer t: rs.getStrictRemove_V(og).getList()) {
				ps.setTimeStamp(t);
				ps.addStrictRemove(og.object, og.group);
			}
		}

		for(OGPair og : rs.getStrictAdd_K().getList()) {
			for(Integer t : rs.getStrictAdd_V(og).getList()) {
				ps.setTimeStamp(t);
				ps.addStrictAdd(og.object, og.group);
			}
		}

		for(SGPair sg : rs.getStrictJoins_K().getList()) {
			for(Integer t : rs.getStrictJoins_V(sg).getList()) {
				ps.setTimeStamp(t);
				ps.addStrictJoin(sg.subject, sg.group);
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
