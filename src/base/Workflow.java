package base;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This represents a particular workflow.
 * @author yechen
 *
 */
public abstract class Workflow implements Measurable {
	public List<Action> executed = new ArrayList<Action>();

	public static final String Q_ACTIONS = "ActionList", Q_INDACTIONS = "IndActionList",
	                           Q_TAILACTIONS = "TailActionList", Q_SATISACTIONS = "SatisfiableActionList", Q_COMPLETE = "Completed";

	public abstract String[] getActionList();

	/**
	 * In some scenario, the satisifiability may require tests onto other components
	 * of the system. In this case, the sys reference allows workflow to use a particular
	 * internal measure of measure class to test the measure they want.
	 */
	public Measurable sys;

	@Override
	public Set<Property> getMeasurables() {
		Set<Property> ret = new HashSet<Property>();
		ret.add(new Property(this, Q_COMPLETE));
		ret.add(new Property(this, Q_ACTIONS));
		ret.add(new Property(this, Q_INDACTIONS));
		ret.add(new Property(this, Q_TAILACTIONS));
		ret.add(new Property(this, Q_SATISACTIONS));
		return ret;
	}

	@Override
	public boolean isMeasurable(Property p) {
		Property qTest = new Property(this, "__test");
		if(!qTest.matchQualifier(p)) {
			return false;
		}
		switch(p.name) {
		case Q_COMPLETE:
			return true;
		case Q_ACTIONS:
			return true;
		case Q_INDACTIONS:
			return true;
		case Q_TAILACTIONS:
			return true;
		case Q_SATISACTIONS:
			return true;
		}
		return false;
	}

	@Override
	public Object getCurMeasure(Property p) {
		Property qTest = new Property(this, "__test");
		if(!qTest.matchQualifier(p)) {
			throw new InvalidMeasureException(p);
		}
		switch(p.name) {
		case Q_COMPLETE:
			return isComplete();
		case Q_ACTIONS:
			return getActionList();
		case Q_INDACTIONS: {
			Set<String> ret = new HashSet<String>();
			for(String actionName : getActionList()) {
				Action a = new Action(actionName, new String[0]);
				if(isIndependentAction(a)) {
					ret.add(a.name);
				}
			}
			return ret;
		}
		case Q_TAILACTIONS: {
			Set<String> ret = new HashSet<String>();
			for(String actionName : getActionList()) {
				Action a = new Action(actionName, new String[0]);
				if(isTailAction(a)) {
					ret.add(a.name);
				}
			}
			return ret;
		}
		case Q_SATISACTIONS: {
			Set<String> ret = new HashSet<String>();
			for(String actionName : getActionList()) {
				Action a = new Action(actionName, new String[0]);
				if(isSatisfiable(a)) {
					ret.add(a.name);
				}
			}
			return ret;
		}
		}
		throw new InvalidMeasureException(p);
	}

	public Action getLastActionExecutedWithName(String actionName) {
		Action ret = null;
		for(Action a : executed) {
			if(a.name.equals(actionName)) {
				ret = a;
			}
		}
		return ret;
	}

	/**
	 * Did we execute an action with name == actionName?
	 * @param actionName
	 * @return true if we did
	 */
	public boolean containsActionWithName(String actionName) {
		for(Action a : executed) {
			if(a.name.equals(actionName)) return true;
		}
		return false;
	}

	/**
	 * How many times did we execute action with name == actionName?
	 * @param actionName
	 * @return number of times we did.
	 */
	public int getNumActionsExecutedWithName(String actionName) {
		int counter = 0;
		for(Action a : executed) {
			if(a.name.equals(actionName)) {
				counter++;
			}
		}
		return counter;
	}

	/**
	 * Whether action a can be executed within this workflow.
	 * @param a the action to be tested.
	 * @return true if it can
	 */
	public abstract boolean isSatisfiable(Action a);

	/**
	 * Did we finish this workflow?
	 * @return true if we did.
	 */
	public boolean isComplete() {
		for(Action a : executed) {
			if(isTailAction(a)) return true;
		}
		return false;
	}

	/**
	 * Is action a marking the end of the workflow?
	 * May be dependent on the state of the workflow. For example, we may require 2 such action a
	 * to be executed before finishing.
	 * @param a
	 * @return true if it is.
	 */
	public abstract boolean isTailAction(Action a);

	/**
	 * Can we start exeucting a without executing anything before in this workflow?
	 * @param a
	 * @return true if we can.
	 */
	public abstract boolean isIndependentAction(Action a);

	/**
	 * Execute action a if a can be executed and workflow is incomplete
	 * @param a the action to be executed.
	 * @return True if successful.
	 */
	public boolean execute(Action a) {
		if(isSatisfiable(a) && !isComplete()) {
			executed.add(a);
			return true;
		}
		return false;
	}

	public String toString() {
		String ret = "";
		for(Action a : executed) {
			ret += a + "=>";
		}
		if(ret.length() > 2) {
			ret = ret.substring(0, ret.length() - 2);
		}
		ret += "=|";
		return ret;
	}
}
