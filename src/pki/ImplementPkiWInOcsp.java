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

//import ibe.PtIbeState.PtKeyRing;
//import skel.Model1State.Model1Ring;

public class ImplementPkiWInOcsp extends Implementation {

//    public static final String SU = "SU";
//    static final String TRUE = PtIbe.TRUE;
//    static final String FALSE = PtIbe.FALSE;

    public static String schemeName() {
        return "OCSP";
    }

    @Override
    public void init(WorkloadState wState) {
        if(!(wState instanceof PkiWState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }
        wScheme = new PkiW(wState);  // workload scheme
        // Set workload's system as well so it can insert and remove actors
        wScheme.sys = sys;

        scheme = new Ocsp(stateMap(wState));  // Ocsp extends Scheme
    }

    @Override
    public State stateMap(WorkloadState wState) {
        if(!(wState instanceof PkiWState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }

//        Rbac0WState rState = (Rbac0WState) wState;
//        PtIbeState iState = new PtIbeState();  // extends State
        PkiWState rState = (PkiWState) wState;
        OcspState iState = new OcspState();

//        iState.addIbeKey(SU, SU);
//
//        for(String u : rState.U) {
//            Log.v("Mapping user " + u + " to initial PT-IBE state");
//            iState.addIbeKey(u, u);
//        }
//
//        for(String p : rState.P) {
//            Log.v("Mapping file " + p + " to initial PT-IBE state");
//            iState.addFile(p);
//            iState.addKey(p, SU, true, true, 1);
//        }
//
//        for(String r : rState.R) {
//            Log.v("Mapping role " + r + " to initial PT-IBE state");
//            iState.addIbeKey(SU, r);
//        }
//
//        for(String u : rState.UA.keySet()) {
//            for(String r : rState.UA.get(u)) {
//                Log.v("Giving key " + r + " to user " + u + " in initial PT-IBE state");
//                iState.addIbeKey(u, r);
//            }
//        }
//
//        for(String p : rState.PA.keySet()) {
//            for(String r : rState.PA.get(p)) {
//                // TODO this assumes only read permission
//                String op = "r";
//                boolean read = (op.equals("r") || op.equals("rw"));
//                boolean write = (op.equals("w") || op.equals("rw"));
//
//                Log.v("Assigning " + p + " to role " + r + " in initial PT-IBE state");
//                iState.addKey(p, r, read, write, 1);
//            }
//        }

        return iState;  // return the state of this model
    }

    @Override
    public void action(Action a) {
//        PtIbe ibe = (PtIbe) scheme;
//        PtIbeState iState = (PtIbeState) ibe.state;
        Ocsp m1 = (Ocsp) scheme;
        OcspState iState = (OcspState) m1.state;
        super.action(a);

        Log.d("Converting action " + a);

        String actor = a.params[0];
//        java.lang.System.out.println("(rwh)ImplementPkiWInOcsp.action(): Performing action on actor  " + actor);
        Log.v("(rwh)ImplementPkiWInOcsp.action(): actor= " + actor + "a.params[1]=" + a.params[1]);

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
//            case "auth": {
//                String u = a.params[1];
//                String f = a.params[2];
//                // TODO for the time being, this assumes 50% r, 50% rw
//                String op = "r";
//                if(Simulation.rand.nextDouble() > 0.5) op = "rw";
//
//                boolean read = (op.equals("r") || op.equals("rw"));
//                boolean write = (op.equals("w") || op.equals("rw"));
//
//                if(write) {
//                    ibe.action(new Action(true, "write", new String[] {actor, u, f}));
//                    // Somewhat of a cheat, assumes that all ids have the same set of keys (no role
//                    // has been left behind without having its keys deleted)
//                    String v = Integer.toString(Collections.max(iState.keys.get(f).get(SU).versions));
//                    ibe.action(new Action("reencryptFile", new String[] {actor, f, v}));
//                } else if(read) {
//                    ibe.action(new Action(true, "read", new String[] {actor, u, f}));
//                }
//
//                break;
//            }
            default:
                throw new RuntimeException("Asked to convert unsupported action " + a);
        }
    }

}