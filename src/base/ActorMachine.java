package base;

/**
 * This is an actor machine. The actor should decide what would happen next.
 *
 *
 */
public abstract class ActorMachine implements Measurable {
	public String actor;
	long time;
	public String actorType;
	public System sys;
	protected String nextAction = null;
	protected Double nextRate = null;

	public static final String Q_PROBACTIONS = "ACTIONPROBS", Q_TIME = "TimeOccupied";

	/**
	 * Determine the next action given the current actor machine description, the current
	 * workflow type, and the parameters.
	 *
	 * Usually the method is doing the following stuff:
	 *
	 * double coin = Simulation.rand.nextDouble() / params.delta;
	 * (flip a coin)
	 * decide what action I should do next.
	 * If my coin toss is bad (> 2.0/60 (2 per min)), then don't execute that acton.
	 * Otherwise, return the action.
	 *
	 *
	 * @param params The parameters used to compute frequency.
	 * @return the next action, may be null (actor idle at this time).
	 * The advance() method must handle the case if no action at all is available.
	 */
	public abstract Action nextAction(Params params);

	/**
	 * Fix the advance() so that it will not advance until certain time elapsed.
	 */
	public void occupy(int t) {
		time += t;
	}

	/**
	 * Optimize advance so it knows where it should return to within the workflow for the next action.
	 * @param action
	 */
	public void transition(Action action) {

	}

	/**
	 * This is used to match the workload state's user storage.
	 * For example, if GMS Users have names u0, u1, ... for actor machine ActorMachineUserInPaper
	 * then ActorMachineUserInPaper must have getPrefix return "u".
	 *
	 * The names of the users must be getPrefix() + index.
	 * @return
	 */
	public abstract String getPrefix();
}
