package base;

import java.util.HashMap;

/**
 * Before starting up each system, the Simulation class needs to feed in initial state and actor machine.
 * Simulation Stater class is responsible for this. Used in Params as a input parameter.
 *
 * Example subclasses: gms.InitGMSUsersClean, testunits.WorkflowDependencyDriver(A/B)Builder, testunits.ActionRateBuilder
 * @author yechen
 *
 */
public abstract class SimulationStarter {

	/**
	 * Build the initial Workload state when starting a system.
	 * @return
	 */
	public abstract WorkloadState build();

	/**
	 * Build a list of actor machines to be created when the system is created.
	 * @return
	 * Key: actor machine class name <br>
	 * Integer: how many such actor machines are there for the given actor machine class.
	 */
	public abstract HashMap<String, Integer> buildActorMachine();
}
