package skel;

import base.Action;
import base.Property;
import base.Workflow;
import base.WorkloadScheme;

public class WorkflowNull extends Workflow {
    private static final long serialVersionUID = 0xFA760A7L;

    @Override
    public String[] getActionList() {
        String[] actions = (String[]) sys.getCurMeasure(new Property(
                WorkloadScheme.class, WorkloadScheme.Q_ACTIONS, new String[0]));
        return actions;
    }

    @Override
    public boolean isSatisfiable(Action a) {
        return true;
        //return executed.size() == 0;
    }

    @Override
    public boolean isTailAction(Action a) {
        return true;
    }

    @Override
    public boolean isIndependentAction(Action a) {
        return true;
    }

}