package gsis;

import java.io.Serializable;
import java.util.HashMap;

import base.SimulationStarter;

public class InitProgramCommitteeClean extends SimulationStarter implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 6306393173125577474L;
	private int users;

	public InitProgramCommitteeClean(int users) {
		this.users = users;
	}

	@Override
	public GsisPcState build() {
		GsisPcState ret = new GsisPcState();
		for(int i = 0; i < users; i++) {
			String username = "u" + i;
			ret.addSubject(username);
		}
		return ret;
	}

	@Override
	public HashMap<String, Integer> buildActorMachine() {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		ret.put("gsis.ActorPCAuthors", users);
		return ret;
	}

}
