package ibe.measure;

import java.util.List;
import java.util.Map;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.Scheme;
import costs.IntSumCost;

import ibe.ImplementRbac0WInPubKey;
import ibe.PubKey;
import ibe.PubKeyState.PubKeyRing;
import ibe.PubKeyState;

public class AsymDecSu extends Measure {
    public AsymDecSu() {
        curMeasurement = new IntSumCost();
    }

    @Override
    public String getPrintFriendlyName() {
        return "Asymmetric decryptions, admin";
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
                    if(a.name.equals("editKeys")) {
                        // Admin decrypts all keys for f, r
                        Property pState = PubKey.lastStateProperty;
                        PubKeyState state = (PubKeyState)w.getCurMeasure(pState);
                        Property pKeys = PubKeyState.keysProperty;
                        Map<String, Map<String, PubKeyRing>> keys = (Map<String, Map<String, PubKeyRing>>)state.getCurMeasure(pKeys);

                        String f = a.params[1];
                        String r = a.params[2];
                        curMeasurement.aggregate(keys.get(f).get(r).versions.size());
                    } else if(a.name.equals("addKey")) {
                        // Every key added by admin is a modified version of an existing key
                        String actor = a.params[0];
                        if(actor.startsWith(ibe.ActorAdmin.prefix())) {
                            curMeasurement.aggregate(1);
                        }
                    } else if(a.name.equals("addRoleKey")) {
                        String u = a.params[1];
                        String r = a.params[2];
                        if(!u.equals(r) && !u.equals(ImplementRbac0WInPubKey.SU)) {
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
