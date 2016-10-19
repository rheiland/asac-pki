package gsis;

import java.io.Serializable;
import java.util.HashMap;

import base.Simulation;
import base.SimulationStarter;

public class InitPlaystationPlusClean extends SimulationStarter implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 6306393173125577474L;
	private int numPublisher, numUser, numObjects, numRegions;

	public InitPlaystationPlusClean(int numPublisher, int numUser, int numObjects, int numRegions) {
		this.numPublisher = numPublisher;
		this.numUser = numUser;
		this.numObjects = numObjects;
		this.numRegions = numRegions;
	}

	@Override
	public GsisPspState build() {
		GsisPspState ret = new GsisPspState();
		for(int i = 0; i < numPublisher; i++) {
			String username = "a" + i;
			ret.addSubject(username);
		}
		for(int i = 0; i < numUser; i++) {
			String username = "u" + i;
			ret.addSubject(username);
		}
		for(int i = 0; i < numObjects; i++) {
			String objectName = "o" + i;
			ret.addObject(objectName);
		}
		for(int i = 0; i < numRegions; i++) {
			String regionName = "r" + i;
			ret.addGroup(regionName);
		}

		// Add 1/2 of the objects each to a random group
		for(int i = 0; i < numObjects/2; i++) {
			ret.addLiberalAdd("o" + i, "r" + Simulation.rand.nextInt(numRegions));
		}

		return ret;
	}

	@Override
	public HashMap<String, Integer> buildActorMachine() {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		ret.put("gsis.ActorSony", numPublisher);
		ret.put("gsis.ActorPlayer", numUser);
		return ret;
	}

}
