package pki;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import base.Action;
import base.Implementation;
import base.SimLogger.Log;
import base.Simulation;
import base.State;
import base.WorkloadState;

public class ImplementPkiWInCrl extends Implementation {

    public static String schemeName() {
        return "CRL";
    }

    @Override
    public void init(WorkloadState wState) {
        if(!(wState instanceof PkiWState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }
        wScheme = new PkiW(wState);  // workload scheme
        // Set workload's system as well so it can insert and remove actors
        wScheme.sys = sys;

        scheme = new Crl(stateMap(wState));  // Crl extends Scheme
    }

    @Override
    public State stateMap(WorkloadState wState) {
        if(!(wState instanceof PkiWState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }

//        Rbac0WState rState = (Rbac0WState) wState;
//        PtIbeState iState = new PtIbeState();  // extends State
        PkiWState rState = (PkiWState) wState;
        CrlState iState = new CrlState();

        return iState;  // return the state of this model
    }

    @Override
    public void action(Action a) {
//        PtIbe ibe = (PtIbe) scheme;
//        PtIbeState iState = (PtIbeState) ibe.state;
        Crl m1 = (Crl) scheme;
        CrlState iState = (CrlState) m1.state;
        super.action(a);

        Log.d("Converting action " + a);

        String actor = a.params[0];
//        java.lang.System.out.println("(rwh)ImplementPkiWInCrl.action(): Performing action on actor  " + actor);
        Log.v("(rwh)ImplementPkiWInCrl.action(): actor= " + actor + "a.params[1]=" + a.params[1]);

        // (rwh) cf. ActorClient.java, e.g. return new Action("checkPathValidation", actor, null, null);
        switch(a.name) {
            case "checkPathValidation": {
                String u = a.params[1];
                break;
            }
            case "checkPathLength": {
                String u = a.params[1];
//                Log.v("Performing action  " + u + " from role " + r);
//                m1.action(new Action("addIbeKey", new String[] {actor, u, u}));
                break;
            }
//            }
            default:
                throw new RuntimeException("Asked to convert unsupported action " + a);
        }
    }

}