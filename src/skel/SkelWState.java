package skel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import base.InvalidMeasureException;
import base.Property;
import base.WorkloadState;

//public class Rbac0WState extends WorkloadState implements Serializable {
public class SkelWState extends WorkloadState implements Serializable {
    private static final long serialVersionUID = 0xFA760A7L;
    // user -> roles
    protected Map<String, Set<String>> UA = new LinkedHashMap<String, Set<String>>();
    // permission -> roles
    protected Map<String, Set<String>> PA = new LinkedHashMap<String, Set<String>>();
    protected HashSet<String> U = new HashSet<String>();
    protected HashSet<String> P = new HashSet<String>();
    protected HashSet<String> R = new HashSet<String>();

    @Override
    public Set<Property> getMeasurables() {
        HashSet<Property> ret = new HashSet<Property>();
        //rwh: what goes here? e.g. SKEL?
        ret.add(new Property(this, "UA", new String[0]));
        ret.add(new Property(this, "PA", new String[0]));
        ret.add(new Property(this, "U", new String[0]));
        ret.add(new Property(this, "P", new String[0]));
        ret.add(new Property(this, "R", new String[0]));
        ret.add(new Property(this, "sizeof", new String[0]));
        ret.addAll(super.getMeasurables());
        return ret;
    }

    @Override
    public boolean isMeasurable(Property p) {
        Property qTest = new Property(this, "__test", new String[0]);
        if(!qTest.matchQualifier(p)) {
            return false;
        }
        switch(p.name) {
            case "UA":
            case "PA":
            case "U":
            case "P":
            case "R":
                return true;
            case "sizeof":
                switch(p.params.get(0)) {
                    case "UA":
                    case "PA":
                    case "U":
                    case "P":
                    case "R":
                    case "ALL":
                        return true;
                }
        }
        return super.isMeasurable(p);
    }

    @Override
    public Object getCurMeasure(Property p) {
        if(super.isMeasurable(p)) {
            return super.getCurMeasure(p);
        }
        Property qTest = new Property(this, "__test", new String[0]);
        if(!qTest.matchQualifier(p)) {
            throw new InvalidMeasureException(p);
        }
        switch(p.name) {
            case "UA":
                return UA;
            case "PA":
                return PA;
            case "U":
                return U;
            case "P":
                return P;
            case "R":
                return R;
            case "sizeof":
                switch(p.params.get(0)) {
                    case "UA":
                        return getMap2Size(UA);
                    case "PA":
                        return getMap2Size(PA);
                    case "U":
                        return U.size();
                    case "P":
                        return P.size();
                    case "R":
                        return R.size();
                    case "ALL":
                        return getMap2Size(UA) + getMap2Size(PA) + U.size() + P.size() + R.size();
                }
        }
        throw new InvalidMeasureException(p);
    }

    protected <T, K> int getMap2Size(Map<T, Set<K>> map) {
        int size = 0;
        for(Object o : map.keySet()) {
            size += 2 * map.get(o).size();
        }
        return size;
    }

    @Override
    public WorkloadState createCopy() {
//        Rbac0WState ret = new Rbac0WState();
        SkelWState ret = new SkelWState();
        ret.copyIn(this);
        return ret;
    }

//    public void copyIn(Rbac0WState old) {
    public void copyIn(SkelWState old) {
        for(String u : old.U) {
            addUser(u);
        }
        for(String r : old.R) {
            addRole(r);
        }
        for(String p : old.P) {
            addPermission(p);
        }
        for(String u : old.UA.keySet()) {
            for(String r : old.UA.get(u)) {
                assignUser(u, r);
            }
        }
        for(String p : old.PA.keySet()) {
            for(String r : old.PA.get(p)) {
                assignPermission(p, r);
            }
        }
    }

    //rwh: this is where all actions (commands, queries) are defined
    public void addUser(String u) {
        if(U.contains(u)) {
            throw new RuntimeException("Attempting to add user " + u + " that already exists.");
        }
        U.add(u);
    }

    public void delUser(String u) {
        if(!U.contains(u)) {
            throw new RuntimeException("Attempting to delete user " + u + " that does not exist.");
        }
        U.remove(u);
        UA.remove(u);
    }

    public void addPermission(String p) {
        if(P.contains(p)) {
            throw new RuntimeException("Attempting to add permission " + p + " that already exists.");
        }
        P.add(p);
    }

    public void delPermission(String p) {
        if(!P.contains(p)) {
            throw new RuntimeException("Attempting to delete permission " + p + " that does not exist.");
        }
        P.remove(p);
        PA.remove(p);
    }

    public void addRole(String r) {
        if(R.contains(r)) {
            throw new RuntimeException("Attempting to add role " + r + " that already exists.");
        }
        R.add(r);
    }

    public void delRole(String r) {
        if(!R.contains(r)) {
            throw new RuntimeException("Attempting to delete role " + r + " that does not exist.");
        }
        R.remove(r);
        for(String u : UA.keySet()) {
            UA.get(u).remove(r);
        }
        for(String p : PA.keySet()) {
            PA.get(p).remove(r);
        }
    }

    public void assignUser(String u, String r) {
        if(!U.contains(u)) {
            throw new RuntimeException("Attempting to assign user " + u + " that does not exist.");
        } else if(!R.contains(r)) {
            throw new RuntimeException("Attempting to assign user to role " + r + " that does not exist.");
        } else if(UA.containsKey(u) && UA.get(u).contains(r)) {
            throw new RuntimeException("Attempting to assign user " + u + " to role " + r + " that is already assigned.");
        }
        if(!UA.containsKey(u)) {
            UA.put(u, new HashSet<String>());
        }
        UA.get(u).add(r);
    }

    public void revokeUser(String u, String r) {
        if(!U.contains(u)) {
            throw new RuntimeException("Attempting to revoke user " + u + " that does not exist.");
        } else if(!R.contains(r)) {
            throw new RuntimeException("Attempting to revoke user from role " + r + " that does not exist.");
        } else if(!UA.containsKey(u) || !UA.get(u).contains(r)) {
            throw new RuntimeException("Attempting to revoke user " + u + " from role " + r + " that is not assigned.");
        }
        UA.get(u).remove(r);
    }

    public void assignPermission(String p, String r) {
        if(!P.contains(p)) {
            throw new RuntimeException("Attempting to assign permission " + p + " that does not exist.");
        } else if(!R.contains(r)) {
            throw new RuntimeException("Attempting to assign permission to role " + r + " that does not exist.");
        } else if(PA.containsKey(p) && PA.get(p).contains(r)) {
            throw new RuntimeException("Attempting to assign permission " + p + " to role " + r + " that is already assigned.");
        }
        if(!PA.containsKey(p)) {
            PA.put(p, new HashSet<String>());
        }
        PA.get(p).add(r);
    }

    public void revokePermission(String p, String r) {
        if(!P.contains(p)) {
            throw new RuntimeException("Attempting to revoke permission " + p + " that does not exist.");
        } else if(!R.contains(r)) {
            throw new RuntimeException("Attempting to revoke permission from role " + r + " that does not exist.");
        } else if(!PA.containsKey(p) || !PA.get(p).contains(r)) {
            throw new RuntimeException("Attempting to revoke permission " + p + " from role " + r + " that is not assigned.");
        }
        PA.get(p).remove(r);
    }

//    public IAccessW<String> getUA_K() {
//        return new IAccessW<String>(UA.keySet(), this, 0);
//    }
//
//    public IAccessW<String> getUA_V(String u) {
//        return new IAccessW<String>(UA.get(u), this, 1);
//    }
//
//    public IAccessW<String> getPA_K() {
//        return new IAccessW<String>(PA.keySet(), this, 0);
//    }
//
//    public IAccessW<String> getPA_V(String p) {
//        return new IAccessW<String>(PA.get(p), this, 1);
//    }
//
//    public IAccessW<String> getU() {
//        return new IAccessW<String>(U, this, 0);
//    }
//
//    public IAccessW<String> getP() {
//        return new IAccessW<String>(P, this, 0);
//    }
//
//    public IAccessW<String> getR() {
//        return new IAccessW<String>(R, this, 0);
//    }

}