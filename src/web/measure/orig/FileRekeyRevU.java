package ibe.measure;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.WorkloadScheme;
import costs.IntReplaceCost;

public class FileRekeyRevU extends Measure {
    Measure rekeys = new SymGenSu();

    public FileRekeyRevU() {
        curMeasurement = new IntReplaceCost();
    }

    @Override
    public String getPrintFriendlyName() {
        return "File rekeys for user revocation";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
        String type = Util.identify(w);

        // Check the implementation
        if(!type.equals("PT")) return;

        // Check the workload action
        Property pWAction = WorkloadScheme.actionProperty;
        Action wAction = (Action) w.getCurMeasure(pWAction);
        if(wAction == null || !wAction.name.equals("revokeUser")) return;

        rekeys.postExecMeasurement(w);
        int encCost = (Integer)rekeys.getCurCost();
        curMeasurement.aggregate(encCost);
    }

    @Override
    public boolean isMeasurementvalid(Measurable w) {
        return rekeys.isMeasurementvalid(w);
    }
}

