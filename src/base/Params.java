package base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * This class is used to store inital paramters.
 *
 * There might be a better way to code this up, but so far I don't see a reason to change it.
 * @author yechen
 *
 */
public class Params implements Serializable {
	private static final long serialVersionUID = -4057058215338139666L;

	/**
	 * What measurements are we concerned about?
	 */
	public HashSet<String> measures = new HashSet<String>();

	public String[] implementationClassNames = {}; //These are implementations we will instantiate in Sim.

	public SimulationStarter starter; //How do we build initial state and actor machine?

	public String workflowName = ""; //The workflow class name to be instantiated.

	// General params
	public double delta;
	public double timelimitPerSimulation;
	public System system = null;

    // IBE params
    public double addBias;
    public double urBias;

    //rwh: Skeleton (skel) params
    public double cost1;
    public int caDepth;

    //rwh: pki params
    public double validationCost;
    public double revocationCost;

	/**
	 * Current run's index, useful for printing in client-server environment.
	 */
	public int runIndex;

	public Params(Params p) {
		measures = new HashSet<String>(p.measures);

		implementationClassNames = p.implementationClassNames;
		starter = p.starter;
		workflowName = p.workflowName;
		delta = p.delta;
		timelimitPerSimulation = p.timelimitPerSimulation;
		system = p.system;

		addBias = p.addBias;
		urBias = p.urBias;

		cost1 = p.cost1;  //rwh
		caDepth = p.caDepth;  //rwh
		validationCost = p.validationCost;  //rwh
		revocationCost = p.revocationCost;  //rwh

		runIndex = p.runIndex;
	}

	public Params() {}
}