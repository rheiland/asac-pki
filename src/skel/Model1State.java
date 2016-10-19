package skel;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import base.InvalidMeasureException;
import base.Property;
import base.State;

//public class PtIbeState extends State {
public class Model1State extends State {
//    // files: file -> symmetric-key version
//    protected Map<String, Integer> files = new LinkedHashMap<String, Integer>();
//    // ibe-encrypted symmetric keys: file -> (role -> rw, versions)
//    protected Map<String, Map<String, PtKeyRing>> keys = new LinkedHashMap<String, Map<String, PtKeyRing>>();
//    // ibe-encrypted ibe keys: identity -> identity (usually user -> role)
//    protected Map<String, Set<String>> ibeKeys = new LinkedHashMap<String, Set<String>>();

//    public class PtKeyRing {
//        public boolean read;
//        public boolean write;
//        public Set<Integer> versions;
//
//        public PtKeyRing(boolean r, boolean w, Integer v) {
//            read = r;
//            write = w;
//            versions = new HashSet<Integer>();
//            versions.add(v);
//        }
//    }

//    public static final Property filesProperty = new Property(PtIbeState.class, "files");
//    public static final Property keysProperty = new Property(PtIbeState.class, "keys");
//    public static final Property ibeKeysProperty = new Property(PtIbeState.class, "ibeKeys");
//    public static final Property sizeofProperty = new Property(PtIbeState.class, "sizeof");
    public static final Property sizeofProperty = new Property(Model1State.class, "sizeof");

//    public static final Property sentinelProperty = filesProperty;

    @Override
    public Set<Property> getMeasurables() {
        HashSet<Property> ret = new HashSet<Property>();
//        ret.add(filesProperty);
//        ret.add(keysProperty);
//        ret.add(ibeKeysProperty);
        ret.add(sizeofProperty);
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
            case "files":
            case "keys":
            case "ibeKeys":
                return true;
            case "sizeof":
                switch(p.params.get(0)) {
                    case "files":
                    case "keys":
                    case "ibeKeys":
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
//            case "files":
//                return files;
//            case "keys":
//                return keys;
//            case "ibeKeys":
//                return ibeKeys;
            case "sizeof":
                switch(p.params.get(0)) {
//                    case "files":
//                        return files.size();
//                    case "keys":
//                        return getKeysSize();
//                    case "ibeKeys":
//                        return getIbeKeysSize();
                    case "ALL":
//                        return files.size() + getKeysSize() + getIbeKeysSize();
                }
        }
        throw new InvalidMeasureException(p);
    }

//    int getKeysSize() {
//        // ibe-encrypted symmetric keys: file -> (role -> rw, versions)
//        // Map<String, Map<String, PtKeyRing>> keys
//        int ret = 0;
//        for(String filename : keys.keySet()) {
//            for(String rolename : keys.get(filename).keySet()) {
//                ret += keys.get(filename).get(rolename).versions.size();
//            }
//        }
//        return ret;
//    }
//
//    int getIbeKeysSize() {
//        // ibe-encrypted ibe keys: identity -> identity (usually user -> role)
//        // Map<String, Set<String>> ibeKeys
//        int ret = 0;
//        for(String identity : ibeKeys.keySet()) {
//            ret += ibeKeys.get(identity).size();
//        }
//        return ret;
//    }

    @Override
    public State createCopy() {
//        PtIbeState ret = new PtIbeState();
        Model1State ret = new Model1State();

//        for(String file : files.keySet()) {
//            ret.addFile(file, files.get(file));
//        }
//        for(String file : keys.keySet()) {
//            for(String identity : keys.get(file).keySet()) {
//                PtKeyRing kr = keys.get(file).get(identity);
//                for(int version: kr.versions) {
//                    ret.addKey(file, identity, kr.read, kr.write, version);
//                }
//            }
//        }
//        for(String user : ibeKeys.keySet()) {
//            ret.addIbeKey(user, user);
//            for(String identity : ibeKeys.get(user)) {
//                if(!identity.equals(user)) {
//                    ret.addIbeKey(user, identity);
//                }
//            }
//        }
        return ret;
    }

//    public void addFile(String f) {
//        if(files.containsKey(f)) {
//            throw new RuntimeException("Attempting to add file " + f + " that already exists.");
//        }
//        files.put(f, 0);
//    }
//
//    public void addFile(String f, int v) {
//        if(files.containsKey(f)) {
//            throw new RuntimeException("Attempting to add file " + f + " that already exists.");
//        }
//        files.put(f, v);
//    }
//
//    public void reencryptFile(String f, int v) {
//        if(!files.containsKey(f)) {
//            throw new RuntimeException("Attempting to reencrypt file " + f + " that does not exist.");
//        }
//        if(files.get(f) > v) {
//            throw new RuntimeException("Attempting to reencrypt file " + f + " from " + files.get(f) + " to " + v);
//        }
//        files.put(f, v);
//    }
//
//    public void deleteFile(String f) {
//        if(!files.containsKey(f)) {
//            throw new RuntimeException("Attempting to delete file " + f + " that does not exist.");
//        }
//        files.remove(f);
//        // TODO: Should we check for remaining keys first?
//        keys.remove(f);
//    }
//
//    public void addKey(String f, String i, boolean r, boolean w, int v) {
//        if(!files.containsKey(f)) {
//            throw new RuntimeException("Attempting to add key for file " + f + " that does not exist.");
//        }
//        if(!keys.containsKey(f)) {
//            keys.put(f, new LinkedHashMap<String, PtKeyRing>());
//        }
//        if(!keys.get(f).containsKey(i)) {
//            keys.get(f).put(i, new PtKeyRing(r, w, v));
//        } else {
//            PtKeyRing kr = keys.get(f).get(i);
//            kr.read = kr.read || r;
//            kr.write = kr.write || w;
//            kr.versions.add(v);
//        }
//    }
//
//    public void editKeys(String f, String i, boolean r, boolean w) {
//        if(!files.containsKey(f)) {
//            throw new RuntimeException("Attempting to edit keys for file " + f + " that does not exist.");
//        }
//        if(!keys.containsKey(f)) {
//            throw new RuntimeException("Attempting to edit keys for file " + f + " without keys.");
//        }
//        if(!keys.get(f).containsKey(i)) {
//            throw new RuntimeException("Attempting to edit keys for (" + f + ", " + i + ") that does not exist.");
//        }
//        PtKeyRing kr = keys.get(f).get(i);
//        kr.read = r;
//        kr.write = w;
//    }
//
//    public void deleteKey(String f, String i, int v) {
//        if(!files.containsKey(f)) {
//            throw new RuntimeException("Attempting to delete key for file " + f + " that does not exist.");
//        }
//        if(!keys.containsKey(f)) {
//            throw new RuntimeException("Attempting to delete key for file " + f + " without keys.");
//        }
//        if(!keys.get(f).containsKey(i)) {
//            throw new RuntimeException("Attempting to delete key (" + f + ", " + i + ") that does not exist.");
//        }
//        PtKeyRing kr = keys.get(f).get(i);
//        kr.versions.remove(v);
//        if(kr.versions.isEmpty()) {
//            keys.get(f).remove(i);
//        }
//    }
//
//    public void addIbeKey(String u, String i) {
//        if(!ibeKeys.containsKey(u)) {
//            if(u.equals(i)) {
//                ibeKeys.put(u, new HashSet<String>());
//            } else {
//                throw new RuntimeException("Attempting to add key " + i + " to user " + u + " without self key.");
//            }
//        }
//        ibeKeys.get(u).add(i);
//    }
//
//    public void deleteIbeKey(String u, String i) {
//        if(u.equals(i)) {
//            throw new RuntimeException("Attempting to delete self key (" + u + ", " + i + ").");
//        }
//        if(!ibeKeys.containsKey(u) || !ibeKeys.get(u).contains(i)) {
//            throw new RuntimeException("Attempting to delete key (" + u + ", " + i + ") that does not exist.");
//        }
//        ibeKeys.get(u).remove(i);
//    }
//
//    public void deleteIbeUser(String u) {
//        if(!ibeKeys.containsKey(u)) {
//            throw new RuntimeException("Attempting to ban user " + u + " that does not exist.");
//        }
//        // TODO: do we need to check for leftover keys?
//        ibeKeys.remove(u);
//    }

}