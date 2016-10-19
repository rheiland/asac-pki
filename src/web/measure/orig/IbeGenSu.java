package ibe.measure;

import java.util.List;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.Scheme;
import costs.IntSumCost;

import ibe.ImplementRbac0WInPtIbe;

public class IbeGenSu extends Measure {
    public IbeGenSu() {
        curMeasurement = new IntSumCost();
    }

    @Override
    public String getPrintFriendlyName() {
        return "IBE key generations, admin";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
        String type = Util.identify(w);

        // Get the actions executed
        Property pActions = Scheme.actionsProperty;
        List<Action> actions = (List<Action>) w.getCurMeasure(pActions);

        if(actions != null && actions.size() > 0) {
            for(Action a: actions) {
                if(type.equals("PT")) {
                    if(a.name.equals("deleteIbeKey")) {
                        curMeasurement.aggregate(1);
                    } else if(a.name.equals("addIbeKey")) {
                        String u = a.params[1];
                        String id = a.params[2];
                        if(u.equals(id) || u.equals(ImplementRbac0WInPtIbe.SU)) {
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
