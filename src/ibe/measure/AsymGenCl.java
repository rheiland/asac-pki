package ibe.measure;

import java.util.List;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.Scheme;
import costs.IntSumCost;

public class AsymGenCl extends Measure {
    public AsymGenCl() {
        curMeasurement = new IntSumCost();
    }

    @Override
    public String getPrintFriendlyName() {
        return "Asymmetric key generations, client";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
        String type = Util.identify(w);

        // Get the actions executed
        Property pActions = Scheme.actionsProperty;
        List<Action> actions = (List<Action>) w.getCurMeasure(pActions);

        if(actions != null && actions.size() > 0) {
            for(Action a: actions) {
                if(type.equals("PK")) {
                    if(a.name.equals("addRoleKey")) {
                        String u = a.params[1];
                        String r = a.params[2];
                        if(u.equals(r)) {
                            curMeasurement.aggregate(1);
                        }
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
