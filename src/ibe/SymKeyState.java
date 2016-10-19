package ibe;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import base.InvalidMeasureException;
import base.Property;
import base.State;

public class SymKeyState extends State {
    protected HashSet<String> U = new HashSet<String>();
    protected HashSet<String> R = new HashSet<String>();
    protected HashSet<String> P = new HashSet<String>();
    // user -> roles
    protected Map<String, Set<String>> UR = new LinkedHashMap<String, Set<String>>();
    // file -> (role -> rw)
    protected Map<String, Map<String, SymKeyRing>> PA = new LinkedHashMap<String, Map<String, SymKeyRing>>();

    public class SymKeyRing {
        public boolean read;
        public boolean write;

        public SymKeyRing(boolean r, boolean w) {
            read = r;
            write = w;
        }
    }

    public static final Property uProperty = new Property(SymKeyState.class, "U");
    public static final Property rProperty = new Property(SymKeyState.class, "R");
    public static final Property pProperty = new Property(SymKeyState.class, "P");
    public static final Property urProperty = new Property(SymKeyState.class, "UR");
    public static final Property paProperty = new Property(SymKeyState.class, "PA");

    public static final Property sentinelProperty = urProperty;

    @Override
    public Set<Property> getMeasurables() {
        HashSet<Property> ret = new HashSet<Property>();
        ret.add(uProperty);
        ret.add(rProperty);
        ret.add(pProperty);
        ret.add(urProperty);
        ret.add(paProperty);
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
            case "U":
            case "R":
            case "P":
            case "UR":
            case "PA":
                return true;
            case "sizeof":
                switch(p.params.get(0)) {
                    case "U":
                    case "R":
                    case "P":
                    case "UR":
                    case "PA":
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
            case "U":
                return U;
            case "R":
                return R;
            case "P":
                return P;
            case "UR":
                return UR;
            case "PA":
                return PA;
            case "sizeof":
                switch(p.params.get(0)) {
                    case "U":
                        return U.size();
                    case "R":
                        return R.size();
                    case "P":
                        return P.size();
                    case "UR":
                        return getUrSize(p);
                    case "PA":
                        return getPaSize(p);
                    case "ALL":
                        return U.size() + R.size() + P.size() + getUrSize(p) + getPaSize(p);
                }
        }
        throw new InvalidMeasureException(p);
    }

    int getUrSize(Property p) {
        throw new InvalidMeasureException(p);
    }

    int getPaSize(Property p) {
        throw new InvalidMeasureException(p);
    }

    @Override
    public State createCopy() {
        SymKeyState ret = new SymKeyState();
        for(String u : U) {
            ret.addUser(u);
        }
        for(String r : R) {
            ret.addRole(r);
        }
        for(String f : P) {
            ret.addFile(f);
        }
        for(String u : UR.keySet()) {
            for(String r : UR.get(u)) {
                ret.assignUser(u, r);
            }
        }
        for(String f : PA.keySet()) {
            for(String r: PA.get(f).keySet()) {
                SymKeyRing kr = PA.get(f).get(r);
                ret.assignPermission(f, r, kr.read, kr.write);
            }
        }
        return ret;
    }

    public void addUser(String u) {
        if(U.contains(u)) {
            throw new RuntimeException("Attempting to add user " + u + " that already exists.");
        }
        U.add(u);
    }

    public void deleteUser(String u) {
        if(!U.contains(u)) {
            throw new RuntimeException("Attempting to delete user " + u + " that does not exist.");
        }
        UR.remove(u);
        U.remove(u);
    }

    public void addRole(String r) {
        if(R.contains(r)) {
            throw new RuntimeException("Attempting to add role " + r + " that already exists.");
        }
        R.add(r);
    }

    public void deleteRole(String r) {
        if(!R.contains(r)) {
            throw new RuntimeException("Attempting to delete role " + r + " that does not exist.");
        }
        for(String u : UR.keySet()) {
            UR.get(u).remove(r);
        }
        for(String f : PA.keySet()) {
            PA.get(f).remove(r);
        }
        R.remove(r);
    }

    public void addFile(String f) {
        if(P.contains(f)) {
            throw new RuntimeException("Attempting to add permission " + f + " that already exists.");
        }
        P.add(f);
    }

    public void deleteFile(String f) {
        if(!P.contains(f)) {
            throw new RuntimeException("Attempting to delete permission " + f + " that does not exist.");
        }
        PA.remove(f);
        P.remove(f);
    }

    public void assignUser(String u, String r) {
        if(!U.contains(u)) {
            throw new RuntimeException("Attempting to assign user " + u + " that does not exist.");
        } else if(!R.contains(r)) {
            throw new RuntimeException("Attempting to assign user to role " + r + " that does not exist.");
        } else if(UR.containsKey(u) && UR.get(u).contains(r)) {
            throw new RuntimeException("Attempting to assign user " + u + " to role " + r + " that is already assigned.");
        }
        if(!UR.containsKey(u)) {
            UR.put(u, new HashSet<String>());
        }
        UR.get(u).add(r);
    }

    public void revokeUser(String u, String r) {
        if(!U.contains(u)) {
            throw new RuntimeException("Attempting to revoke user " + u + " that does not exist.");
        } else if(!R.contains(r)) {
            throw new RuntimeException("Attempting to revoke user from role " + r + " that does not exist.");
        } else if(!UR.containsKey(u) || !UR.get(u).contains(r)) {
            throw new RuntimeException("Attempting to revoke user " + u + " from role " + r + " that is not assigned.");
        }
        UR.get(u).remove(r);
    }

    public void assignPermission(String f, String role, boolean r, boolean w) {
        if(!P.contains(f)) {
            throw new RuntimeException("Attempting to assign permission " + f + " that does not exist.");
        } else if(!R.contains(role)) {
            throw new RuntimeException("Attempting to assign permission to role " + role + " that does not exist.");
        } else if(PA.containsKey(f) && PA.get(f).containsKey(role)) {
            SymKeyRing kr = PA.get(f).get(role);
            kr.read = kr.read || r;
            kr.write = kr.write || w;
            return;
        }
        if(!PA.containsKey(f)) {
            PA.put(f, new LinkedHashMap<String, SymKeyRing>());
        }
        PA.get(f).put(role, new SymKeyRing(r, w));
    }

    public void revokePermission(String f, String role, boolean r, boolean w) {
        if(!P.contains(f)) {
            throw new RuntimeException("Attempting to revoke permission " + f + " that does not exist.");
        } else if(!R.contains(role)) {
            throw new RuntimeException("Attempting to revoke permission from role " + role + " that does not exist.");
        } else if(!PA.containsKey(f) || !PA.get(f).containsKey(role)) {
            throw new RuntimeException("Attempting to revoke permission " + f + " from role " + role + " that is not assigned.");
        }
        SymKeyRing kr = PA.get(f).get(role);
        // If role will maintain some access to f after
        if((kr.read && !r) || (kr.write && !w)) {
            kr.read = kr.read && !r;
            kr.write = kr.write && !w;
        } else {
            PA.get(f).remove(role);
        }
    }

}

