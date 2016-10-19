package base;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the scheme to be simulating the other one.
 * @author yechen
 *
 */
public abstract class WorkloadScheme implements Measurable {
	public WorkloadState state;
	protected WorkloadState prevState;
	protected Map<String, Object> curMeasures = new HashMap<String, Object>();
	private Action lastExecuted = null;
	protected Boolean lastQueryResult = new Boolean(false);
	public boolean noReset = false;
	public System sys;

	/**
	 * This query returns Integer which will represent time the actor is busy executing the last action.
	 * Q_TIME cannot be resolved here. It should probably be coded in either Measure or subclass.
	 */
	public static final String Q_LASTACTION="LastAction",
	                           Q_LASTSTATE="LastState", Q_ACTIONS = "ActionList", Q_CLASSNAME="ClassName",
	                           Q_LASTQUERYRESULT = "LastQueryResult";

	// common properties
	public static final Property actionProperty = new Property(WorkloadScheme.class, Q_LASTACTION);

	/**
	 * Given a workload action, execute it within workload (not AC)
	 * Subclasses must override this method, and they should call this super method first.
	 * @param the workload action to be executed in workload.
	 */
	public void action(Action action) {
		lastExecuted = action;
		if(noReset) return;
		prevState = state.createCopy();
		state.newWorkloadCommand();
	}

	/**
	 * Constructor of the Workload Scheme takes in the initial Workload state as parameter.
	 * @param initState
	 */
	public WorkloadScheme(WorkloadState initState) {
		state = initState;
	}

	@Override
	public Set<Property> getMeasurables() {
		HashSet<Property> ret = new HashSet<Property>();
		ret.add(new Property(this, Q_LASTACTION));
		ret.add(new Property(this, Q_LASTSTATE));
		ret.add(new Property(this, Q_CLASSNAME));
		ret.add(new Property(this, Q_LASTQUERYRESULT));
		ret.addAll(state.getMeasurables());
		return ret;
	}

	@Override
	public boolean isMeasurable(Property p) {
		Property qTest = new Property(this, "__test");
		if(!qTest.matchQualifier(p)) {
			qTest = new Property(state.getClass(), "__test");
			if(!qTest.matchQualifier(p)) {
				return false;
			} else {
				return state.isMeasurable(p);
			}
		}
		switch(p.name) {
		case Q_LASTACTION:
		case Q_LASTSTATE:
		case Q_CLASSNAME:
		case Q_LASTQUERYRESULT:
			return true;
		}
		return false;
	}

	@Override
	public Object getCurMeasure(Property p) {
		if(state.isMeasurable(p)) {
			return state.getCurMeasure(p);
		}
		Property qTest = new Property(this, "__test");
		if(!qTest.matchQualifier(p)) {
			throw new InvalidMeasureException(p);
		}
		switch(p.name) {
		case Q_LASTACTION:
			return lastExecuted;
		case Q_LASTSTATE:
			return prevState;
		case Q_CLASSNAME:
			return this.getClass().getName();
		case Q_LASTQUERYRESULT:
			return lastQueryResult;
		}
		throw new InvalidMeasureException(p);
	}

	/**
	 * Given action a and its context w, fill in the parameters based on Workload Scheme state and w.
	 * @param a
	 * @param w
	 * @return the new action with missing parameters filled in.
	 */
	public abstract Action addParameters(Action a, Workflow w);

}
