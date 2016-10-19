package ibe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import base.Action;
import base.InvalidMeasureException;
import base.Property;
import base.Scheme;
import base.State;
import base.WorkloadScheme;

public class SymKey extends Scheme {

    public SymKey(State initState) {
        super(initState);

        if(!(initState instanceof SymKeyState)) {
            throw new RuntimeException("Incorrect state type: " + initState.getClass().getName());
        }
    }

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public static final Property lastStateProperty = new Property(SymKey.class, Scheme.Q_LASTSTATE, new String[0]);

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
        SymKeyState iState = (SymKeyState) state;
        switch(acAction.name) {
            case "addUser":
                iState.addUser(acAction.params[1]);
                break;
            case "deleteUser":
                iState.deleteUser(acAction.params[1]);
                break;
            case "addRole":
                iState.addRole(acAction.params[1]);
                break;
            case "deleteRole":
                iState.deleteRole(acAction.params[1]);
                break;
            case "addFile":
                iState.addFile(acAction.params[1]);
                break;
            case "deleteFile":
                iState.deleteFile(acAction.params[1]);
                break;
            case "assignUser":
                iState.assignUser(acAction.params[1], acAction.params[2]);
                break;
            case "revokeUser":
                iState.revokeUser(acAction.params[1], acAction.params[2]);
                break;
            case "assignPermission":
                iState.assignPermission(acAction.params[1], acAction.params[2],
                        acAction.params[3] == TRUE, acAction.params[4] == TRUE);
                break;
            case "revokePermission":
                iState.revokePermission(acAction.params[1], acAction.params[2],
                        acAction.params[3] == TRUE, acAction.params[4] == TRUE);
                break;
            default:
                throw new RuntimeException("Unrecognized command: " + acAction);
        }
    }

    private void query(Action acAction) {
        SymKeyState iState = (SymKeyState) state;
        switch(acAction.name) {
            case "read": {
                String u = acAction.params[1];
                String f = acAction.params[2];

                if(!iState.UR.containsKey(u) || !iState.PA.containsKey(f)) {
                    lastQueryResult = false;
                    return;
                }

                Set<String> uRole = iState.UR.get(u);
                Set<String> fRole = iState.PA.get(f).keySet();
                lastQueryResult = !Collections.disjoint(uRole, fRole);
                return;
            }
            case "write": {
                String u = acAction.params[1];
                String f = acAction.params[2];

                if(!iState.UR.containsKey(u) || !iState.PA.containsKey(f)) {
                    lastQueryResult = false;
                    return;
                }

                Set<String> uRole = iState.UR.get(u);
                Set<String> fRole = new HashSet<String>();

                for(String role : iState.PA.get(f).keySet()) {
                    if(iState.PA.get(f).get(role).write) {
                        fRole.add(role);
                    }
                }
                lastQueryResult = !Collections.disjoint(uRole, fRole);
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
        "addUser", "deleteUser", "addRole", "deleteRole", "addFile", "deleteFile", "assignUser",
        "revokeUser", "assignPermission", "revokePermission", "read", "write"
    };
}
