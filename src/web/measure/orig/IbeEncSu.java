package ibe.measure;

import java.util.List;
import java.util.Map;
import java.util.Set;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.Scheme;
import costs.IntSumCost;

import ibe.PtIbe;
import ibe.PtIbeState.PtKeyRing;
import ibe.PtIbeState;

public class IbeEncSu extends Measure {
    public IbeEncSu() {
        curMeasurement = new IntSumCost();
    }

    @Override
    public String getPrintFriendlyName() {
        return "IBE encryptions, admin";
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
                        // Admin reencrypts all keys for f, id
                        Property pState = PtIbe.lastStateProperty;
                        PtIbeState state = (PtIbeState)w.getCurMeasure(pState);
                        Property pKeys = PtIbeState.keysProperty;
                        Map<String, Map<String, PtKeyRing>> keys = (Map<String, Map<String, PtKeyRing>>)state.getCurMeasure(pKeys);

                        String f = a.params[1];
                        String id = a.params[2];
                        curMeasurement.aggregate(keys.get(f).get(id).versions.size());
                    } else if(a.name.equals("addIbeKey")) {
                        String u = a.params[1];
                        String id = a.params[2];
                        if(!u.equals(id)) {
                            curMeasurement.aggregate(1);
                        }
                    } else if(a.name.equals("addKey")) {
                        String actor = a.params[0];
                        if(actor.startsWith(ibe.ActorAdmin.prefix())) {
                            curMeasurement.aggregate(1);
                        }
                    } else if(a.name.equals("deleteIbeKey")) {
                        // Encrypt new role key for all remaining members
                        Property pState = PtIbe.lastStateProperty;
                        PtIbeState state = (PtIbeState)w.getCurMeasure(pState);
                        Property pIbeKeys = PtIbeState.ibeKeysProperty;
                        Map<String, Set<String>> ibeKeys = (Map<String, Set<String>>)state.getCurMeasure(pIbeKeys);

                        String id = a.params[2];
                        for(String u : ibeKeys.keySet()) {
                            if(ibeKeys.get(u).contains(id)) {
                                curMeasurement.aggregate(1);
                            }
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
