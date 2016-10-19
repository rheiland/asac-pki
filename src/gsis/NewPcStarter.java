package gsis;

import java.io.Serializable;
import java.util.HashMap;

import base.SimulationStarter;

public class NewPcStarter extends SimulationStarter implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 6306393173125577474L;
	private int numrev;
	private int numauth;

	public NewPcStarter(int numrev, int numauth) {
		this.numrev = numrev;
		this.numauth = numauth;
	}

	@Override
	public GsisPcState build() {
		GsisPcState ret = new GsisPcState();
		ret.addSubject("c1");
		for(int i = 0; i < numrev; i++) {
			String username = "r" + i;
			ret.addSubject(username);
		}
		for(int i = 0; i < numauth; i++) {
			String username = "a" + i;
			ret.addSubject(username);
		}
		return ret;
	}

	@Override
	public HashMap<String, Integer> buildActorMachine() {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		ret.put("gsis.ActorNewPcChair", 1);
		ret.put("gsis.ActorNewPcReviewer", numrev);
		ret.put("gsis.ActorNewPcAuthor", numauth);
		return ret;
	}

}
