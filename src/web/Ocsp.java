package web;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import base.Action;
import base.InvalidMeasureException;
import base.Property;
import base.Scheme;
import base.State;
import base.WorkloadScheme;

//import ibe.PubKeyState.PubKeyRing;
//import web.OcspState.CertRing;

public class Ocsp extends Scheme {

    public Ocsp(State initState) {
        super(initState);

        if(!(initState instanceof OcspState)) {
            throw new RuntimeException("Incorrect state type: " + initState.getClass().getName());
        }
    }

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public static final Property lastStateProperty = new Property(Ocsp.class, Scheme.Q_LASTSTATE, new String[0]);

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
        OcspState iState = (OcspState) state;
        switch(acAction.name) {
            case "addFile":
                iState.addFile(acAction.params[1]);
                break;
            case "reencryptFile":
                iState.reencryptFile(acAction.params[1], Integer.parseInt(acAction.params[2]));
                break;
            case "deleteFile":
                iState.deleteFile(acAction.params[1]);
                break;
            case "addKey":
                iState.addKey(acAction.params[1], acAction.params[2], acAction.params[3] == TRUE,
                        acAction.params[4] == TRUE, Integer.parseInt(acAction.params[5]));
                break;
            case "editKeys":
                iState.editKeys(acAction.params[1], acAction.params[2], acAction.params[3] == TRUE,
                        acAction.params[4] == TRUE);
                break;
            case "deleteKey":
                iState.deleteKey(acAction.params[1], acAction.params[2],
                        Integer.parseInt(acAction.params[3]));
                break;
            case "addRoleKey":
                iState.addRoleKey(acAction.params[1], acAction.params[2]);
                break;
            case "deleteRoleKey":
                iState.deleteRoleKey(acAction.params[1], acAction.params[2]);
                break;
            case "deleteUser":
                iState.deleteUser(acAction.params[1]);
                break;
            case "fakeSymKeyGen":
                break;
            default:
                throw new RuntimeException("Unrecognized command: " + acAction);
        }
    }

    private void query(Action acAction) {
        OcspState iState = (OcspState) state;
        switch(acAction.name) {
            case "read": {
                // TODO: Make this query safe
                String u = acAction.params[1];
                String f = acAction.params[2];
                int v = iState.files.get(f);
                Set<String> uid = new HashSet<String>(iState.roleKeys.get(u));
                Set<String> fid = iState.keys.get(f).keySet();
                uid.retainAll(fid);
                lastQueryResult = !uid.isEmpty();
                return;
            }
            case "write": {
                // TODO: Make this query safe
//                String u = acAction.params[1];
//                String f = acAction.params[2];
//                int v = iState.files.get(f);
//                Set<String> uid = new HashSet<String>(iState.roleKeys.get(u));
//                Map<String, CertRing> fkeys = iState.keys.get(f);
//                Set<String> fid = new HashSet<String>();
//                for(String id : fkeys.keySet()) {
//                    if(fkeys.get(id).write) {
//                        fid.add(id);
//                    }
//                }
//                uid.retainAll(fid);
//                lastQueryResult = !uid.isEmpty();
//                return;
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
        "addFile", "reencryptFile", "deleteFile", "addKey", "editKeys", "deleteKey", "addRoleKey",
        "deleteRoleKey", "deleteUser", "fakeSymKeyGen", "read", "write"
    };
}
