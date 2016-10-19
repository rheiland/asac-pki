package ibe;

import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Set;

import base.Action;
import base.Implementation;
import base.State;
import base.WorkloadState;
import base.SimLogger.Log;

public class ImplementRbac0WInCrIbe extends Implementation {

    static final String SU = "SU";

    public static String cat(String ... strs) {
        String ret = "";
        for(String s : strs) {
            ret += s + ":";
        }
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    public static String schemeName() {
        return "CR-IBE";
    }

    @Override
    public void init(WorkloadState wState) {
        if(!(wState instanceof Rbac0WState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }
        wScheme = new Rbac0W(wState);
        // Set workload's system as well so it can insert and remove actors
        wScheme.sys = sys;
        scheme = new CrIbe(stateMap(wState));
    }

    @Override
    public State stateMap(WorkloadState wState) {
        if(!(wState instanceof Rbac0WState)) {
            throw new RuntimeException("Workload state of incorrect type: " + wState.getClass().getName());
        }

        Rbac0WState rState = (Rbac0WState) wState;
        CrIbeState iState = new CrIbeState();

        for(String u : rState.U) {
            Log.v("Mapping user " + u + " to initial CR-IBE state");
            iState.addUser(u);
        }
        iState.addUser(SU);

        for(String u : rState.UA.keySet()) {
            for(String r : rState.UA.get(u)) {
                Log.v("Giving key " + r + " to user " + u + " in initial CR-IBE state");
                iState.giveIbeKey(u, r);
            }
        }

        for(String r : rState.R) {
            Log.v("Mapping role " + r + " to initial CR-IBE state");
            iState.giveIbeKey(SU, r);
        }

        for(String p : rState.PA.keySet()) {
            for(String r : rState.PA.get(p)) {
                String file = cat(p, r);
                Log.v("Encrypting " + file + " to role " + r + " in initial CR-IBE state");
                iState.addFile(file);
                iState.encrypt(file, r);
            }
        }

        return iState;
    }

    private static void unsupportedAction(Action a) {
        throw new RuntimeException("Asked to convert unsupported action " + a);
    }

    @Override
    public void action(Action a) {
        CrIbe ibe = (CrIbe) scheme;
        CrIbeState iState = (CrIbeState) ibe.state;
        super.action(a);

        String u, r, p, file;

        Log.d("Converting action " + a);

        switch(a.name) {
            case "addU": {
                ibe.action(new Action("addUser", a.params));
                break;
            }
            case "delU": {
                // This is CR-IBE, this will be expensive
                u = a.params[1];
                // This will throw some exception if the user doesn't exist.
                Set<String> currentRoles = new LinkedHashSet<String>(iState.UI.get(u));
                for(String role : currentRoles) {
                    ibe.action(new Action("revokeIbeKey", new String[] {null, u, role}));
                }
                ibe.action(new Action("deleteUser", a.params));
                //unsupportedAction(a);
                break;
            }
            case "addR": {
                r = a.params[1];
                ibe.action(new Action("giveIbeKey", new String[] {null, SU, r}));
                break;
            }
            case "delR": {
                // This is CR-IBE, this will be expensive
                r = a.params[1];
                // First we delete every file that the identity can access. Right now, no file is
                // accessible by more than one identity, but if this assumption changes, this needs
                // to be more robust (and we likely need to count the cost for this differently,
                // since we'll need to do some rekeying)
                Set<String> currentFiles = new LinkedHashSet<String>(iState.AI.keySet());
                for(String f : currentFiles) {
                    if(iState.AI.get(f).contains(r)) {
                        ibe.action(new Action("deleteFile", new String[] {null, f}));
                    }
                }
                ibe.action(new Action("purgeIbeKey", a.params));
                //unsupportedAction(a);
                break;
            }
            case "addP": {
                // Thought this would be the following, but I guess it's nothing
                //ibe.action(new Action("addFile", a.params));
                break;
            }
            case "delP": {
                p = a.params[1];
                Set<String> currentFiles = new LinkedHashSet<String>(iState.A);
                for(String f : currentFiles) {
                    if(f.startsWith(cat(p, ""))) {
                        ibe.action(new Action("deleteFile", new String[] {null, f}));
                    }
                }
                break;
            }
            case "assignUser": {
                ibe.action(new Action("giveIbeKey", a.params));
                break;
            }
            case "revokeUser": {
                // This is CR-IBE, this will be expensive
                ibe.action(new Action("revokeIbeKey", a.params));
                //unsupportedAction(a);
                break;
            }
            case "assignPermission": {
                r = a.params[1];
                p = a.params[2];
                file = cat(p, r);

                ibe.action(new Action("addFile", new String[] {null, file}));
                ibe.action(new Action("encrypt", new String[] {null, file, r}));
                break;
            }
            case "revokePermission": {
                r = a.params[1];
                p = a.params[2];
                file = cat(p, r);

                // This will throw a NullPointerException when trying to revoke a permission that
                // has not been granted. I'm choosing not to catch it, since that indicates a bug in
                // the implementation or the implementation being used wrong. If this assumption
                // changes, we should re-enable the try-catch.
                //try{
                if(!iState.AI.get(file).contains(r)) {
                    // If for some reason we try to revoke a permission, and the r:p file has not
                    // been granted the p permission, just throw an error and die. There is no
                    // reason this should happen short of an implementation bug.
                    String err = "Error converting action " + a + "\n";
                    err += "\t" + file + " only encrypted for " + iState.AI.get(file);
                    throw new RuntimeException(err);
                }
                //} catch(NullPointerException e) {
                //        System.err.println("Error revoking permission " + p + " from " + r);
                //        e.printStackTrace();
                //}

                ibe.action(new Action("deleteFile", new String[] {null, file}));
                break;
            }
            case "auth": {
                u = a.params[1];
                p = a.params[2];

                Set<String> currentFiles = new LinkedHashSet<String>(iState.AI.keySet());
                if(currentFiles.size() < 1) break;

                boolean found = false;
                for(String f : currentFiles) {
                    if(f.startsWith(cat(p, ""))) {
                        Set<String> intersection = new HashSet<String>(iState.AI.get(f));
                        intersection.retainAll(iState.UI.get(u));
                        if(!intersection.isEmpty()) {
                            ibe.action(new Action(true, "auth", new String[] {null, u, f}));
                            found = true;
                            break;
                        }
                    }
                }

                if(!found) {
                    // If we didn't find a file that matched, then it's getting denied anyway
                    ibe.action(new Action(true, "auth", new String[] {null, u, "REJECT"}));
                }

                break;
            }
            default:
                unsupportedAction(a);
                break;
        }
    }

}
