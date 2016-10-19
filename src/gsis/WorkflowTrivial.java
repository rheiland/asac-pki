package gsis;

import base.Action;
import base.Property;
import base.Workflow;
import base.WorkloadScheme;

public class WorkflowTrivial extends Workflow {

	/**
	 *
	 */
	private static final long serialVersionUID = 1322566771830039501L;

	@Override
	public String[] getActionList() {
		String[] actions = (String[]) sys.getCurMeasure(new Property(
		                           WorkloadScheme.class, WorkloadScheme.Q_ACTIONS, new String[0]));
		return actions;
	}

	@Override
	public boolean isSatisfiable(Action a) {
		if(isIndependentAction(a)) {
			return executed.size() == 0;
		}
		if(containsActionWithName("SJoin") || containsActionWithName("LJoin")) {
			if(a.name.endsWith("Add")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isTailAction(Action a) {
		if(a.name.endsWith("Join")) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isIndependentAction(Action a) {
		return true;
	}

}
