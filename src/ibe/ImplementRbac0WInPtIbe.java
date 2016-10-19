package ibe;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import base.Action;
import base.Implementation;
import base.SimLogger.Log;
import base.Simulation;
import base.State;
import base.WorkloadState;

import ibe.PtIbeState.PtKeyRing;

public class ImplementRbac0WInPtIbe extends Implementation {

    public static final String SU = "SU";
    static final String TRUE = PtIbe.TRUE;
    static final String FALSE = PtIbe.FALSE;

    public static String schemeName() {
        return "Model 2";
    }

    @Override
    public void init(WorkloadState wState) {
        if(!(wState instanceof Rbac0WState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }
        wScheme = new Rbac0W(wState);
        // Set workload's system as well so it can insert and remove actors
        wScheme.sys = sys;
        scheme = new PtIbe(stateMap(wState));
    }

    @Override
    public State stateMap(WorkloadState wState) {
        if(!(wState instanceof Rbac0WState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }

        Rbac0WState rState = (Rbac0WState) wState;
        PtIbeState iState = new PtIbeState();

        iState.addIbeKey(SU, SU);

        for(String u : rState.U) {
            Log.v("Mapping user " + u + " to initial PT-IBE state");
            iState.addIbeKey(u, u);
        }

        for(String p : rState.P) {
            Log.v("Mapping file " + p + " to initial PT-IBE state");
            iState.addFile(p);
            iState.addKey(p, SU, true, true, 1);
        }

        for(String r : rState.R) {
            Log.v("Mapping role " + r + " to initial PT-IBE state");
            iState.addIbeKey(SU, r);
        }

        for(String u : rState.UA.keySet()) {
            for(String r : rState.UA.get(u)) {
                Log.v("Giving key " + r + " to user " + u + " in initial PT-IBE state");
                iState.addIbeKey(u, r);
            }
        }

        for(String p : rState.PA.keySet()) {
            for(String r : rState.PA.get(p)) {
                // TODO this assumes only read permission
                String op = "r";
                boolean read = (op.equals("r") || op.equals("rw"));
                boolean write = (op.equals("w") || op.equals("rw"));

                Log.v("Assigning " + p + " to role " + r + " in initial PT-IBE state");
                iState.addKey(p, r, read, write, 1);
            }
        }

        return iState;
    }

    @Override
    public void action(Action a) {
        PtIbe ibe = (PtIbe) scheme;
        PtIbeState iState = (PtIbeState) ibe.state;
        super.action(a);

        Log.d("Converting action " + a);

        String actor = a.params[0];

        switch(a.name) {
            case "addU": {
                String u = a.params[1];
                ibe.action(new Action("addIbeKey", new String[] {actor, u, u}));
                break;
            }
            case "delU": {
                String u = a.params[1];
                // This will throw some exception if the user doesn't exist.
                Set<String> roles = new LinkedHashSet<String>(iState.ibeKeys.get(u));
                roles.remove(u);
                for(String r : roles) {
                    Log.v("Removing " + u + " from role " + r);
                    ibe.action(new Action("deleteIbeKey", new String[] {actor, u, r}));
                }

                Set<String> files = new LinkedHashSet<String>();
                // find all f where <f, r, ?, ?, ?> in keys for any r in roles
                for(String f : iState.keys.keySet()) {
                    if(!Collections.disjoint(iState.keys.get(f).keySet(), roles)) {
                        files.add(f);
                    }
                }
                for(String f : files) {
                    ibe.action(new Action("fakeSymKeyGen", new String[] {actor}));

                    roles = new LinkedHashSet<String>(iState.keys.get(f).keySet());
                    for(String q : roles) {
                        String read = iState.keys.get(f).get(q).read ? TRUE : FALSE;
                        String write = iState.keys.get(f).get(q).write ? TRUE : FALSE;
                        String v = Integer.toString(Collections.max(iState.keys.get(f).get(q).versions) + 1);
                        ibe.action(new Action("addKey", new String[] {actor, f, q, read, write, v}));
                    }
                }

                ibe.action(new Action("deleteIbeUser", new String[] {actor, u}));
                break;
            }
            case "addR": {
                String r = a.params[1];
                ibe.action(new Action("addIbeKey", new String[] {actor, SU, r}));
                break;
            }
            case "delR": {
                String r = a.params[1];
                Set<String> files = new LinkedHashSet<String>();
                // find all f where <f, r, ?, ?, ?> in keys
                for(String f : iState.keys.keySet()) {
                    if(iState.keys.get(f).containsKey(r)) {
                        files.add(f);
                    }
                }
                for(String f : files) {
                    ibe.action(new Action("fakeSymKeyGen", new String[] {actor}));

                    Set<Integer> versions = new LinkedHashSet<Integer>(iState.keys.get(f).get(r).versions);
                    for(Integer v : versions) {
                        ibe.action(new Action("deleteKey", new String[] {actor, f, r, Integer.toString(v)}));
                    }
                    Set<String> roles = new LinkedHashSet<String>(iState.keys.get(f).keySet());
                    for(String q : roles) {
                        String read = iState.keys.get(f).get(q).read ? TRUE : FALSE;
                        String write = iState.keys.get(f).get(q).write ? TRUE : FALSE;
                        String v = Integer.toString(Collections.max(iState.keys.get(f).get(q).versions) + 1);
                        ibe.action(new Action("addKey", new String[] {actor, f, q, read, write, v}));
                    }
                }
                ibe.action(new Action("deleteIbeKey", new String[] {actor, SU, r}));
                break;
            }
            case "addP": {
                String f = a.params[1];
                ibe.action(new Action("addFile", new String[] {actor, f}));
                ibe.action(new Action("addKey", new String[] {actor, f, SU, TRUE, TRUE, Integer.toString(1)}));
                break;
            }
            case "delP": {
                String f = a.params[1];
                Set<String> roles = new LinkedHashSet<String>(iState.keys.get(f).keySet());
                for(String r : roles) {
                    Set<Integer> versions = new LinkedHashSet<Integer>(iState.keys.get(f).get(r).versions);
                    for(Integer v : versions) {
                        ibe.action(new Action("deleteKey", new String[] {actor, f, r, Integer.toString(v)}));
                    }
                }
                ibe.action(new Action("deleteFile", new String[] {actor, f}));
                break;
            }
            case "assignUser": {
                String u = a.params[1];
                String r = a.params[2];
                ibe.action(new Action("addIbeKey", new String[] {actor, u, r}));
                break;
            }
            case "revokeUser": {
                String u = a.params[1];
                String r = a.params[2];
                ibe.action(new Action("deleteIbeKey", new String[] {actor, u, r}));

                Set<String> files = new LinkedHashSet<String>();
                // find all f where <f, r, ?, ?, ?> in keys
                for(String f : iState.keys.keySet()) {
                    if(iState.keys.get(f).containsKey(r)) {
                        files.add(f);
                    }
                }
                for(String f : files) {
                    ibe.action(new Action("fakeSymKeyGen", new String[] {actor}));

                    Set<String> roles = new LinkedHashSet<String>(iState.keys.get(f).keySet());
                    for(String q : roles) {
                        String read = iState.keys.get(f).get(q).read ? TRUE : FALSE;
                        String write = iState.keys.get(f).get(q).write ? TRUE : FALSE;
                        String v = Integer.toString(Collections.max(iState.keys.get(f).get(q).versions) + 1);
                        ibe.action(new Action("addKey", new String[] {actor, f, q, read, write, v}));
                    }
                }

                break;
            }
            case "assignPermission": {
                String f = a.params[1];
                String r = a.params[2];
                // TODO for the time being, this always assigns read
                String op = "r";

                boolean read = (op.equals("r") || op.equals("rw"));
                boolean write = (op.equals("w") || op.equals("rw"));

                if(iState.keys.get(f).containsKey(r)) {
                    read = read || iState.keys.get(f).get(r).read;
                    write = write || iState.keys.get(f).get(r).write;
                    String sRead = read ? TRUE : FALSE;
                    String sWrite = write ? TRUE : FALSE;
                    ibe.action(new Action("editKeys", new String[] {actor, f, r, sRead, sWrite}));
                } else {
                    Set<Integer> versions = new LinkedHashSet<Integer>(iState.keys.get(f).get(SU).versions);
                    for(Integer v : versions) {
                        String sRead = read ? TRUE : FALSE;
                        String sWrite = write ? TRUE : FALSE;
                        String sV = Integer.toString(v);
                        ibe.action(new Action("addKey", new String[] {actor, f, r, sRead, sWrite, sV}));
                    }
                }
                break;
            }
            case "revokePermission": {
                String f = a.params[1];
                String r = a.params[2];
                // TODO for the time being, this always revokes both read, write
                String op = "rw";

                boolean read = (op.equals("r") || op.equals("rw"));
                boolean write = (op.equals("w") || op.equals("rw"));

                PtKeyRing kr = iState.keys.get(f).get(r);

                boolean remainRead = kr.read && !read;
                boolean remainWrite = kr.write && !write;

                if(remainRead || remainWrite) {
                    String sRead = remainRead ? TRUE : FALSE;
                    String sWrite = remainWrite ? TRUE : FALSE;
                    ibe.action(new Action("editKeys", new String[] {actor, f, r, sRead, sWrite}));
                } else {
                    ibe.action(new Action("fakeSymKeyGen", new String[] {actor}));

                    Set<Integer> versions = new LinkedHashSet<Integer>(kr.versions);
                    for(Integer v : versions) {
                        ibe.action(new Action("deleteKey", new String[] {actor, f, r, Integer.toString(v)}));
                    }
                    Set<String> roles = new LinkedHashSet<String>(iState.keys.get(f).keySet());
                    for(String q : roles) {
                        String sRead = iState.keys.get(f).get(q).read ? TRUE : FALSE;
                        String sWrite = iState.keys.get(f).get(q).write ? TRUE : FALSE;
                        String v = Integer.toString(Collections.max(iState.keys.get(f).get(q).versions) + 1);
                        ibe.action(new Action("addKey", new String[] {actor, f, q, sRead, sWrite, v}));
                    }
                }

                break;
            }
            case "auth": {
                String u = a.params[1];
                String f = a.params[2];
                // TODO for the time being, this assumes 50% r, 50% rw
                String op = "r";
                if(Simulation.rand.nextDouble() > 0.5) op = "rw";

                boolean read = (op.equals("r") || op.equals("rw"));
                boolean write = (op.equals("w") || op.equals("rw"));

                if(write) {
                    ibe.action(new Action(true, "write", new String[] {actor, u, f}));
                    // Somewhat of a cheat, assumes that all ids have the same set of keys (no role
                    // has been left behind without having its keys deleted)
                    String v = Integer.toString(Collections.max(iState.keys.get(f).get(SU).versions));
                    ibe.action(new Action("reencryptFile", new String[] {actor, f, v}));
                } else if(read) {
                    ibe.action(new Action(true, "read", new String[] {actor, u, f}));
                }

                break;
            }
            default:
                throw new RuntimeException("Asked to convert unsupported action " + a);
        }
    }

}

