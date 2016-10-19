package ibe.measure;

import java.util.List;
import java.util.Map;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.Scheme;
import costs.IntSumCost;

import ibe.ImplementRbac0WInPtIbe;
import ibe.PtIbe;
import ibe.PtIbeState.PtKeyRing;
import ibe.PtIbeState;

public class IbeDecSu extends Measure {
    public IbeDecSu() {
        curMeasurement = new IntSumCost();
    }

    @Override
    public String getPrintFriendlyName() {
        return "IBE decryptions, admin";
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
                    if(a.name.equals("editKeys")) {
                        // Admin decrypts all keys for f, id
                        Property pState = PtIbe.lastStateProperty;
                        PtIbeState state = (PtIbeState)w.getCurMeasure(pState);
                        Property pKeys = PtIbeState.keysProperty;
                        Map<String, Map<String, PtKeyRing>> keys = (Map<String, Map<String, PtKeyRing>>)state.getCurMeasure(pKeys);

                        String f = a.params[1];
                        String id = a.params[2];
                        curMeasurement.aggregate(keys.get(f).get(id).versions.size());
                    } else if(a.name.equals("addKey")) {
                        // Every key added by admin is a modified version of an existing key
                        String actor = a.params[0];
                        if(actor.startsWith(ibe.ActorAdmin.prefix())) {
                            curMeasurement.aggregate(1);
                        }
                    } else if(a.name.equals("addIbeKey")) {
                        String u = a.params[1];
                        String id = a.params[2];
                        if(!u.equals(id) && !u.equals(ImplementRbac0WInPtIbe.SU)) {
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
