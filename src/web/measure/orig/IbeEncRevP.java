package ibe.measure;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.WorkloadScheme;
import costs.IntReplaceCost;

public class IbeEncRevP extends Measure {
    Measure enc = new IbeEncSu();

    public IbeEncRevP() {
        curMeasurement = new IntReplaceCost();
    }

    @Override
    public String getPrintFriendlyName() {
        return "IBE encryptions for permission revocation";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
        String type = Util.identify(w);

        // Check the implementation
        if(!type.equals("PT")) return;

        // Check the workload action
        Property pWAction = WorkloadScheme.actionProperty;
        Action wAction = (Action) w.getCurMeasure(pWAction);
        if(wAction == null || !wAction.name.equals("revokePermission")) return;

        enc.postExecMeasurement(w);
        int encCost = (Integer)enc.getCurCost();
        curMeasurement.aggregate(encCost);
    }

    @Override
    public boolean isMeasurementvalid(Measurable w) {
        return enc.isMeasurementvalid(w);
    }
}

