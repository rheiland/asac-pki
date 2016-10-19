package ibe;

import java.util.HashSet;
import java.util.Set;

import base.Action;
import base.InvalidMeasureException;
import base.Property;
import base.Scheme;
import base.SimLogger.Log;
import base.State;
import base.WorkloadScheme;

public class CrIbe extends Scheme {

    public CrIbe(State initState) {
        super(initState);

        if(!(initState instanceof CrIbeState)) {
            throw new RuntimeException("Incorrect state type: " + initState.getClass().getName());
        }
    }

    @Override
    public boolean isMeasurable(Property p) {
        if(super.isMeasurable(p)) return true;
        Property qTest = new Property(this, "__test", new String[0]);
        if(!qTest.matchQualifier(p)) {
            return false;
        }
        if(p.name.equals(Q_ACTIONS)) {
            return true;
        }
        return false;
    }

    @Override
    public Set<Property> getMeasurables() {
        HashSet<Property> ret = new HashSet<Property>();
        ret.addAll(super.getMeasurables());
        ret.add(new Property(this, Q_ACTIONS, new String[0]));
        return ret;
    }

    @Override
    public Object getCurMeasure(Property p) {
        try {
            if(super.isMeasurable(p)) {
                return super.getCurMeasure(p);
            }
        } catch(InvalidMeasureException e) {
            e.printStackTrace();
        }
        Property qTest = new Property(this, "__test", new String[0]);
        if(!qTest.matchQualifier(p)) {
            throw new InvalidMeasureException(p);
        }
        switch(p.name) {
            // TODO Should this work? WorkloadScheme??
            case WorkloadScheme.Q_ACTIONS:
                return AVAIL_ACTIONS;
        }
        throw new InvalidMeasureException(p);
    }

    private void command(Action acAction) {
        CrIbeState iState = (CrIbeState) state;
        switch(acAction.name) {
            case "addUser":
                iState.addUser(acAction.params[1]);
                break;
            case "deleteUser":
                iState.deleteUser(acAction.params[1]);
                break;
            case "addFile":
                iState.addFile(acAction.params[1]);
                break;
            case "deleteFile":
                iState.deleteFile(acAction.params[1]);
                break;
            case "giveIbeKey":
                iState.giveIbeKey(acAction.params[1], acAction.params[2]);
                break;
            case "revokeIbeKey":
                iState.revokeIbeKey(acAction.params[1], acAction.params[2]);
                break;
            case "encrypt":
                iState.encrypt(acAction.params[1], acAction.params[2]);
                break;
            // Special command for removing a role the hard way
            case "purgeIbeKey":
                iState.purgeIbeKey(acAction.params[1]);
                break;
            default:
                throw new RuntimeException("Unrecognized command: " + acAction);
        }
    }

    private void query(Action acAction) {
        CrIbeState iState = (CrIbeState) state;
        switch(acAction.name) {
            case "UI": {
                String u = acAction.params[1];
                String i = acAction.params[2];
                if(iState.U.contains(u) && iState.UI.containsKey(u)) {
                    lastQueryResult = iState.UI.get(u).contains(i);
                } else {
                    Log.d("Query mismatch: UI with non-existent user " + u);
                    lastQueryResult = false;
                }
                return;
            }
            case "A": {
                String a = acAction.params[1];
                lastQueryResult = iState.A.contains(a);
                return;
            }
            case "auth": {
                String u = acAction.params[1];
                String a = acAction.params[2];
                if(iState.U.contains(u) && iState.UI.containsKey(u)) {
                    if(iState.A.contains(a) && iState.AI.containsKey(a)) {
                        Set<String> intersection = new HashSet<String>(iState.UI.get(u));
                        intersection.retainAll(iState.AI.get(a));
                        lastQueryResult = !intersection.isEmpty();
                    } else {
                        Log.d("Query mismatch: auth with non-existent file " + a);
                        lastQueryResult = false;
                    }
                } else {
                    Log.d("Query mismatch: auth with non-existent user " + u);
                    lastQueryResult = false;
                }
                return;
            }
        }
    }

    @Override
    public void action(Action acAction) {
        super.action(acAction);
        if(acAction.isQuery) {
            query(acAction);
        } else {
            command(acAction);
        }

    }

    public static final String[] AVAIL_ACTIONS = {
        "addUser", "deleteUser", "addFile", "deleteFile",
        "giveIbeKey", "revokeIbeKey", "encrypt",
        "purgeIbeKey",
        "UI", "A", "auth"
    };
}
