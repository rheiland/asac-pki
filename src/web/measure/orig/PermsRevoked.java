package ibe.measure;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.WorkloadScheme;
import costs.IntSumCost;

public class PermsRevoked extends Measure {

    public PermsRevoked() {
        curMeasurement = new IntSumCost();
    }

    @Override
    public String getPrintFriendlyName() {
        return "Number of permission revocations";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
        // Get the workload action
        Property p = WorkloadScheme.actionProperty;
        Action a = (Action) w.getCurMeasure(p);
        if(a != null) {
            if(a.name.equals("revokePermission")) {
                curMeasurement.aggregate(1);
            }
        }
    }

    @Override
    public boolean isMeasurementvalid(Measurable w) {
        Property p = WorkloadScheme.actionProperty;
        return w.isMeasurable(p);
    }
}

