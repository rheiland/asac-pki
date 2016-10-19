package gsis;

import java.io.Serializable;
import java.util.HashMap;

import base.SimulationStarter;

public class InitTopClean extends SimulationStarter implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 6306393173125577474L;
	private int numManagers, numPosters, numConsumers;

	public InitTopClean(int numManagers, int numPosters, int numConsumers) {
		this.numConsumers = numConsumers;
		this.numManagers = numManagers;
		this.numPosters = numPosters;
	}

	@Override
	public GsisTopState build() {
		GsisTopState ret = new GsisTopState();
		for(int i = 0; i < numPosters; i++) {
			String username = "p" + i;
			ret.addSubject(username);
		}
		for(int i = 0; i < numManagers; i++) {
			String username = "m" + i;
			ret.addSubject(username);
		}
		for(int i = 0; i < numConsumers; i++) {
			String username = "c" + i;
			ret.addSubject(username);
		}
		return ret;
	}

	@Override
	public HashMap<String, Integer> buildActorMachine() {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		ret.put("gsis.ActorPoster", numPosters);
		ret.put("gsis.ActorManager", numManagers);
		ret.put("gsis.ActorReader", numConsumers);
		return ret;
	}

}
