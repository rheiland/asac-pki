package ibe.measure;

import java.util.List;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.Scheme;
import costs.IntSumCost;

public class SymDecCl extends Measure {
    public SymDecCl() {
        curMeasurement = new IntSumCost();
    }

    @Override
    public String getPrintFriendlyName() {
        return "Symmetric decryptions, client";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
        String type = Util.identify(w);

        // Get the actions executed
        Property pActions = Scheme.actionsProperty;
        List<Action> actions = (List<Action>) w.getCurMeasure(pActions);

        if(actions != null && actions.size() > 0) {
            for(Action a: actions) {
                if(type.equals("PT") || type.equals("PK")) {
                    if(a.name.equals("reencryptFile") || a.name.equals("read")) {
                        curMeasurement.aggregate(1);
                    }
                } else if(type.equals("SD")) {
                    if(a.name.equals("read") || a.name.equals("write")) {
                        curMeasurement.aggregate(1);
                    }
                }
            }
        }
    }

    @Override
    public boolean isMeasurementvalid(Measurable w) {
        Property p = Scheme.actionsProperty;
        return w.isMeasurable(p);
    }

}
