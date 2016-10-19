package ibe;

import base.Action;
import base.Implementation;
import base.SimLogger.Log;
import base.Simulation;
import base.State;
import base.WorkloadState;

public class ImplementRbac0WInSymKey extends Implementation {

    static final String TRUE = SymKey.TRUE;
    static final String FALSE = SymKey.FALSE;

    public static String schemeName() {
        return "Model 4";
    }

    @Override
    public void init(WorkloadState wState) {
        if(!(wState instanceof Rbac0WState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }
        wScheme = new Rbac0W(wState);
        // Set workload's system as well so it can insert and remove actors
        wScheme.sys = sys;
        scheme = new SymKey(stateMap(wState));
    }

    @Override
    public State stateMap(WorkloadState wState) {
        if(!(wState instanceof Rbac0WState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }

        Rbac0WState rState = (Rbac0WState) wState;
        SymKeyState pState = new SymKeyState();

        for(String u : rState.U) {
            Log.v("Mapping user " + u + " to initial SymKey state");
            pState.addUser(u);
        }

        for(String r : rState.R) {
            Log.v("Mapping role " + r + " to initial SymKey state");
            pState.addRole(r);
        }

        for(String p : rState.P) {
            Log.v("Mapping file " + p + " to initial SymKey state");
            pState.addFile(p);
        }

        for(String u : rState.UA.keySet()) {
            for(String r : rState.UA.get(u)) {
                Log.v("Giving key " + r + " to user " + u + " in initial SymKey state");
                pState.assignUser(u, r);
            }
        }

        for(String p : rState.PA.keySet()) {
            for(String r : rState.PA.get(p)) {
                // TODO this assumes only read permission
                String op = "r";
                boolean read = (op.equals("r") || op.equals("rw"));
                boolean write = (op.equals("w") || op.equals("rw"));

                Log.v("Assigning " + p + " to role " + r + " in initial SymKey state");
                pState.assignPermission(p, r, read, write);
            }
        }

        return pState;
    }

    @Override
    public void action(Action a) {
        SymKey symkey = (SymKey) scheme;
        SymKeyState pState = (SymKeyState) symkey.state;
        super.action(a);

        Log.d("Converting action " + a);

        String actor = a.params[0];

        switch(a.name) {
            case "addU": {
                String u = a.params[1];
                symkey.action(new Action("addUser", new String[] {actor, u}));
                break;
            }
            case "delU": {
                String u = a.params[1];
                symkey.action(new Action("deleteUser", new String[] {actor, u}));
                break;
            }
            case "addR": {
                String r = a.params[1];
                symkey.action(new Action("addRole", new String[] {actor, r}));
                break;
            }
            case "delR": {
                String r = a.params[1];
                symkey.action(new Action("deleteRole", new String[] {actor, r}));
                break;
            }
            case "addP": {
                String f = a.params[1];
                symkey.action(new Action("addFile", new String[] {actor, f}));
                break;
            }
            case "delP": {
                String f = a.params[1];
                symkey.action(new Action("deleteFile", new String[] {actor, f}));
                break;
            }
            case "assignUser": {
                String u = a.params[1];
                String r = a.params[2];
                symkey.action(new Action("assignUser", new String[] {actor, u, r}));
                break;
            }
            case "revokeUser": {
                String u = a.params[1];
                String r = a.params[2];
                symkey.action(new Action("revokeUser", new String[] {actor, u, r}));
                break;
            }
            case "assignPermission": {
                String f = a.params[1];
                String r = a.params[2];
                // TODO for the time being, this always assigns read
                String op = "r";

                boolean read = (op.equals("r") || op.equals("rw"));
                boolean write = (op.equals("w") || op.equals("rw"));
                String sRead = read ? TRUE : FALSE;
                String sWrite = write ? TRUE : FALSE;

                symkey.action(new Action("assignPermission", new String[] {actor, f, r, sRead, sWrite}));
                break;
            }
            case "revokePermission": {
                String f = a.params[1];
                String r = a.params[2];
                // TODO for the time being, this always revokes both read, write
                String op = "rw";

                boolean read = (op.equals("r") || op.equals("rw"));
                boolean write = (op.equals("w") || op.equals("rw"));
                String sRead = read ? TRUE : FALSE;
                String sWrite = write ? TRUE : FALSE;

                symkey.action(new Action("revokePermission", new String[] {actor, f, r, sRead, sWrite}));
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
                    symkey.action(new Action(true, "write", new String[] {actor, u, f}));
                } else if(read) {
                    symkey.action(new Action(true, "read", new String[] {actor, u, f}));
                }

                break;
            }
            default:
                throw new RuntimeException("Asked to convert unsupported action " + a);
        }
    }

}

