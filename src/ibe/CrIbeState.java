package ibe;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import base.InvalidMeasureException;
import base.Property;
import base.State;

public class CrIbeState extends State {
    // users
    protected HashSet<String> U = new HashSet<String>();
    // user -> identities
    protected Map<String, Set<String>> UI = new LinkedHashMap<String, Set<String>>();
    // files
    protected HashSet<String> A = new HashSet<String>();
    // file -> identities
    protected Map<String, Set<String>> AI = new LinkedHashMap<String, Set<String>>();

    @Override
    public Set<Property> getMeasurables() {
        HashSet<Property> ret = new HashSet<Property>();
        ret.add(new Property(this, "U", new String[0]));
        ret.add(new Property(this, "UI", new String[0]));
        ret.add(new Property(this, "A", new String[0]));
        ret.add(new Property(this, "AI", new String[0]));
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
            case "U":
            case "UI":
            case "A":
            case "AI":
                return true;
            case "sizeof":
                switch(p.params.get(0)) {
                    case "U":
                    case "UI":
                    case "A":
                    case "AI":
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
            case "UI":
                return UI;
            case "A":
                return A;
            case "AI":
                return AI;
            case "sizeof":
                switch(p.params.get(0)) {
                    case "U":
                        return U.size();
                    case "UI":
                        return getMapSetSize(UI);
                    case "A":
                        return A.size();
                    case "AI":
                        return getMapSetSize(AI);
                    case "ALL":
                        return U.size() + getMapSetSize(UI) + A.size() + getMapSetSize(AI);
                }
        }
        throw new InvalidMeasureException(p);
    }

    <K, V> int getMapSetSize(Map<K, Set<V>> map) {
        int size = 0;
        for(K key : map.keySet()) {
            Set<V> values = map.get(key);
            for(V value : values) {
                size += 1;
            }
        }
        return size;
    }

    @Override
    public State createCopy() {
        CrIbeState ret = new CrIbeState();
        for(String u : U) {
            ret.addUser(u);
        }
        for(String u : UI.keySet()) {
            for(String i : UI.get(u)) {
                ret.giveIbeKey(u, i);
            }
        }
        for(String a : A) {
            ret.addFile(a);
        }
        for(String a : AI.keySet()) {
            for(String i : AI.get(a)) {
                ret.encrypt(a, i);
            }
        }
        return ret;
    }

    // TODO none of the actions below consider accessio

    public void addUser(String u) {
        U.add(u);
        UI.put(u, new HashSet<String>());
    }

    public void deleteUser(String u) {
        U.remove(u);
        UI.remove(u);
    }

    public void addFile(String a) {
        A.add(a);
        AI.put(a, new HashSet<String>());
    }

    public void deleteFile(String a) {
        A.remove(a);
        AI.remove(a);
    }

    public void giveIbeKey(String u, String i) {
        try {
            UI.get(u).add(i);
        } catch(NullPointerException e) {
            // Don't give keys to non-existent users, but don't crash over it
            e.printStackTrace();
        }
    }

    public void revokeIbeKey(String u, String i) {
        try {
            UI.get(u).remove(i);
        } catch(NullPointerException e) {
            // Don't revoke keys from non-existent users, but don't crash over it
            e.printStackTrace();
        }
    }

    public void encrypt(String a, String i) {
        try {
            AI.get(a).add(i);
        } catch(NullPointerException e) {
            // Don't encrypt non-existent files, but don't crash over it
            e.printStackTrace();
        }
    }

    /**
     * This must be called after the files have already been deleted!!!
     */
    public void purgeIbeKey(String i) {
        for(String u : UI.keySet()) {
            if(UI.get(u).contains(i)) {
                revokeIbeKey(u, i);
            }
        }
        // Here we delete every file that the identity could access. Right now, no file is
        // accessible by more than one identity, but if this assumption changes, this needs to be
        // more robust (and we likely need to count the cost for this differently, since we'll need
        // to do some rekeying)
        //
        // We don't do this anymore because the implementation does it now, which is easier to count
        //
        //for(String a : AI.keySet()) {
        //    if(AI.get(a).contains(i)) {
        //        deleteFile(a);
        //    }
        //}
    }

}
