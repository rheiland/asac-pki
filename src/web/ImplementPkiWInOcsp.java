package web;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import base.Action;
import base.Implementation;
import base.SimLogger.Log;
import base.Simulation;
import base.State;
import base.WorkloadState;

//import ibe.PubKeyState.PubKeyRing;
import web.CertState.CertRing;

public class ImplementPkiWInOcsp extends Implementation {

    public static final String SU = "SU";
    static final String TRUE = Cert.TRUE;
    static final String FALSE = Cert.FALSE;

    public static String schemeName() {
        return "Ocsp";
//        return "Model 3";
    }

    @Override
    public void init(WorkloadState wState) {
        if(!(wState instanceof PkiWState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }
        wScheme = new PkiW(wState);
        // Set workload's system as well so it can insert and remove actors
        wScheme.sys = sys;
        scheme = new Cert(stateMap(wState));
        // from  ImplementRbac0WInPtIbe
//        scheme = new PtIbe(stateMap(wState));

    }

    @Override
    public State stateMap(WorkloadState wState) {
        if(!(wState instanceof PkiWState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }

        PkiWState rState = (PkiWState) wState;
        CertState pState = new CertState();

        pState.addRoleKey(SU, SU);

        for(String u : rState.U) {
            Log.v("Mapping user " + u + " to initial Cert state");
            pState.addRoleKey(u, u);
        }

        for(String p : rState.P) {
            Log.v("Mapping file " + p + " to initial Cert state");
            pState.addFile(p);
            pState.addKey(p, SU, true, true, 1);
        }

        for(String r : rState.R) {
            Log.v("Mapping role " + r + " to initial Cert state");
            pState.addRoleKey(SU, r);
        }

        for(String u : rState.UA.keySet()) {
            for(String r : rState.UA.get(u)) {
                Log.v("Giving key " + r + " to user " + u + " in initial Cert state");
                pState.addRoleKey(u, r);
            }
        }

        for(String p : rState.PA.keySet()) {
            for(String r : rState.PA.get(p)) {
                // TODO this assumes only read permission
                String op = "r";
                boolean read = (op.equals("r") || op.equals("rw"));
                boolean write = (op.equals("w") || op.equals("rw"));

                Log.v("Assigning " + p + " to role " + r + " in initial Cert state");
                pState.addKey(p, r, read, write, 1);
            }
        }
        return pState;
    }

    @Override
    public void action(Action a) {
        Cert pubkey = (Cert) scheme;
        CertState pState = (CertState) pubkey.state;
        super.action(a);

        Log.d("Converting action " + a);
        String actor = a.params[0];

        switch(a.name) {
            case "addU": {
                String u = a.params[1];
                pubkey.action(new Action("addRoleKey", new String[] {actor, u, u}));
                break;
            }
            case "delU": {
                String u = a.params[1];
                // This will throw some exception if the user doesn't exist.
                Set<String> roles = new LinkedHashSet<String>(pState.roleKeys.get(u));
                roles.remove(u);
                for(String r : roles) {
                    Log.v("Removing " + u + " from role " + r);
                    pubkey.action(new Action("deleteRoleKey", new String[] {actor, u, r}));
                }

                Set<String> files = new LinkedHashSet<String>();
                // find all f where <f, r, ?, ?, ?> in keys for any r in roles
                for(String f : pState.keys.keySet()) {
                    if(!Collections.disjoint(pState.keys.get(f).keySet(), roles)) {
                        files.add(f);
                    }
                }
                for(String f : files) {
                    pubkey.action(new Action("fakeSymKeyGen", new String[] {actor}));

                    roles = new LinkedHashSet<String>(pState.keys.get(f).keySet());
                    for(String q : roles) {
                        String read = pState.keys.get(f).get(q).read ? TRUE : FALSE;
                        String write = pState.keys.get(f).get(q).write ? TRUE : FALSE;
                        String v = Integer.toString(Collections.max(pState.keys.get(f).get(q).versions) + 1);
                        pubkey.action(new Action("addKey", new String[] {actor, f, q, read, write, v}));
                    }
                }

                pubkey.action(new Action("deleteUser", new String[] {actor, u}));
                break;
            }
            case "addR": {
                String r = a.params[1];
                pubkey.action(new Action("addRoleKey", new String[] {actor, SU, r}));
                break;
            }
            case "delR": {
                String r = a.params[1];
                Set<String> files = new LinkedHashSet<String>();
                // find all f where <f, r, ?, ?, ?> in keys
                for(String f : pState.keys.keySet()) {
                    if(pState.keys.get(f).containsKey(r)) {
                        files.add(f);
                    }
                }
                for(String f : files) {
                    pubkey.action(new Action("fakeSymKeyGen", new String[] {actor}));

                    Set<Integer> versions = new LinkedHashSet<Integer>(pState.keys.get(f).get(r).versions);
                    for(Integer v : versions) {
                        pubkey.action(new Action("deleteKey", new String[] {actor, f, r, Integer.toString(v)}));
                    }
                    Set<String> roles = new LinkedHashSet<String>(pState.keys.get(f).keySet());
                    for(String q : roles) {
                        String read = pState.keys.get(f).get(q).read ? TRUE : FALSE;
                        String write = pState.keys.get(f).get(q).write ? TRUE : FALSE;
                        String v = Integer.toString(Collections.max(pState.keys.get(f).get(q).versions) + 1);
                        pubkey.action(new Action("addKey", new String[] {actor, f, q, read, write, v}));
                    }
                }
                pubkey.action(new Action("deleteRoleKey", new String[] {actor, SU, r}));
                break;
            }
            case "addP": {
                String f = a.params[1];
                pubkey.action(new Action("addFile", new String[] {actor, f}));
                pubkey.action(new Action("addKey", new String[] {actor, f, SU, TRUE, TRUE, Integer.toString(1)}));
                break;
            }
            case "delP": {
                String f = a.params[1];
                Set<String> roles = new LinkedHashSet<String>(pState.keys.get(f).keySet());
                for(String r : roles) {
                    Set<Integer> versions = new LinkedHashSet<Integer>(pState.keys.get(f).get(r).versions);
                    for(Integer v : versions) {
                        pubkey.action(new Action("deleteKey", new String[] {actor, f, r, Integer.toString(v)}));
                    }
                }
                pubkey.action(new Action("deleteFile", new String[] {actor, f}));
                break;
            }
            case "assignUser": {
                String u = a.params[1];
                String r = a.params[2];
                pubkey.action(new Action("addRoleKey", new String[] {actor, u, r}));
                break;
            }
            case "revokeUser": {
                String u = a.params[1];
                String r = a.params[2];
                pubkey.action(new Action("deleteRoleKey", new String[] {actor, u, r}));

                Set<String> files = new LinkedHashSet<String>();
                // find all f where <f, r, ?, ?, ?> in keys
                for(String f : pState.keys.keySet()) {
                    if(pState.keys.get(f).containsKey(r)) {
                        files.add(f);
                    }
                }
                for(String f : files) {
                    pubkey.action(new Action("fakeSymKeyGen", new String[] {actor}));

                    Set<String> roles = new LinkedHashSet<String>(pState.keys.get(f).keySet());
                    for(String q : roles) {
                        String read = pState.keys.get(f).get(q).read ? TRUE : FALSE;
                        String write = pState.keys.get(f).get(q).write ? TRUE : FALSE;
                        String v = Integer.toString(Collections.max(pState.keys.get(f).get(q).versions) + 1);
                        pubkey.action(new Action("addKey", new String[] {actor, f, q, read, write, v}));
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

                if(pState.keys.get(f).containsKey(r)) {
                    read = read || pState.keys.get(f).get(r).read;
                    write = write || pState.keys.get(f).get(r).write;
                    String sRead = read ? TRUE : FALSE;
                    String sWrite = write ? TRUE : FALSE;
                    pubkey.action(new Action("editKeys", new String[] {actor, f, r, sRead, sWrite}));
                } else {
                    Set<Integer> versions = new LinkedHashSet<Integer>(pState.keys.get(f).get(SU).versions);
                    for(Integer v : versions) {
                        String sRead = read ? TRUE : FALSE;
                        String sWrite = write ? TRUE : FALSE;
                        String sV = Integer.toString(v);
                        pubkey.action(new Action("addKey", new String[] {actor, f, r, sRead, sWrite, sV}));
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

                CertRing kr = pState.keys.get(f).get(r);

                boolean remainRead = kr.read && !read;
                boolean remainWrite = kr.write && !write;

                if(remainRead || remainWrite) {
                    String sRead = remainRead ? TRUE : FALSE;
                    String sWrite = remainWrite ? TRUE : FALSE;
                    pubkey.action(new Action("editKeys", new String[] {actor, f, r, sRead, sWrite}));
                } else {
                    pubkey.action(new Action("fakeSymKeyGen", new String[] {actor}));

                    Set<Integer> versions = new LinkedHashSet<Integer>(kr.versions);
                    for(Integer v : versions) {
                        pubkey.action(new Action("deleteKey", new String[] {actor, f, r, Integer.toString(v)}));
                    }
                    Set<String> roles = new LinkedHashSet<String>(pState.keys.get(f).keySet());
                    for(String q : roles) {
                        String sRead = pState.keys.get(f).get(q).read ? TRUE : FALSE;
                        String sWrite = pState.keys.get(f).get(q).write ? TRUE : FALSE;
                        String v = Integer.toString(Collections.max(pState.keys.get(f).get(q).versions) + 1);
                        pubkey.action(new Action("addKey", new String[] {actor, f, q, sRead, sWrite, v}));
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
                    pubkey.action(new Action(true, "write", new String[] {actor, u, f}));
                    // Somewhat of a cheat, assumes that all ids have the same set of keys (no role
                    // has been left behind without having its keys deleted)
                    String v = Integer.toString(Collections.max(pState.keys.get(f).get(SU).versions));
                    pubkey.action(new Action("reencryptFile", new String[] {actor, f, v}));
                } else if(read) {
                    pubkey.action(new Action(true, "read", new String[] {actor, u, f}));
                }
                break;
            }
            default:
                throw new RuntimeException("Asked to convert unsupported action " + a);
        }
    }
}