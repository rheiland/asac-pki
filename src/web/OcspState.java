package web;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import base.InvalidMeasureException;
import base.Property;
import base.State;

public class OcspState extends State {
    // files: file -> symmetric-key version
    protected Map<String, Integer> files = new LinkedHashMap<String, Integer>();
    // asym-encrypted symmetric keys: file -> (role -> rw, versions)
    protected Map<String, Map<String, CertRing>> keys = new LinkedHashMap<String, Map<String, CertRing>>();
    // asym-encrypted role keys: user -> role
    protected Map<String, Set<String>> roleKeys = new LinkedHashMap<String, Set<String>>();

    public class CertRing {
        public boolean read;
        public boolean write;
        public Set<Integer> versions;

        public CertRing(boolean r, boolean w, Integer v) {
            read = r;
            write = w;
            versions = new HashSet<Integer>();
            versions.add(v);
        }
    }

    public static final Property filesProperty = new Property(CertState.class, "files");
    public static final Property keysProperty = new Property(CertState.class, "keys");
    public static final Property roleKeysProperty = new Property(CertState.class, "roleKeys");
    public static final Property sizeofProperty = new Property(CertState.class, "sizeof");

    public static final Property sentinelProperty = filesProperty;

    @Override
    public Set<Property> getMeasurables() {
        HashSet<Property> ret = new HashSet<Property>();
        ret.add(filesProperty);
        ret.add(keysProperty);
        ret.add(roleKeysProperty);
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
            case "roleKeys":
                return true;
            case "sizeof":
                switch(p.params.get(0)) {
                    case "files":
                    case "keys":
                    case "roleKeys":
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
            case "files":
                return files;
            case "keys":
                return keys;
            case "roleKeys":
                return roleKeys;
            case "sizeof":
                switch(p.params.get(0)) {
                    case "files":
                        return files.size();
                    case "keys":
                        return getKeysSize();
                    case "roleKeys":
                        return getRoleKeysSize();
                    case "ALL":
                        return files.size() + getKeysSize() + getRoleKeysSize();
                }
        }
        throw new InvalidMeasureException(p);
    }

    int getKeysSize() {
        // asym-encrypted symmetric keys: file -> (role -> rw, versions)
        // Map<String, Map<String, CertRing>> keys
        int ret = 0;
        for(String filename : keys.keySet()) {
            for(String rolename : keys.get(filename).keySet()) {
                ret += keys.get(filename).get(rolename).versions.size();
            }
        }
        return ret;
    }

    int getRoleKeysSize() {
        // asym-encrypted role keys: user -> role
        // Map<String, Set<String>> roleKeys
        int ret = 0;
        for(String user : roleKeys.keySet()) {
            ret += roleKeys.get(user).size();
        }
        return ret;
    }

    @Override
    public State createCopy() {
        CertState ret = new CertState();
        for(String file : files.keySet()) {
            ret.addFile(file, files.get(file));
        }
        for(String file : keys.keySet()) {
            for(String role : keys.get(file).keySet()) {
                CertRing kr = keys.get(file).get(role);
                for(int version: kr.versions) {
                    ret.addKey(file, role, kr.read, kr.write, version);
                }
            }
        }
        for(String user : roleKeys.keySet()) {
            ret.addRoleKey(user, user);
            for(String role : roleKeys.get(user)) {
                if(!role.equals(user)) {
                    ret.addRoleKey(user, role);
                }
            }
        }
        return ret;
    }

    public void addFile(String f) {
        if(files.containsKey(f)) {
            throw new RuntimeException("Attempting to add file " + f + " that already exists.");
        }
        files.put(f, 0);
    }

    public void addFile(String f, int v) {
        if(files.containsKey(f)) {
            throw new RuntimeException("Attempting to add file " + f + " that already exists.");
        }
        files.put(f, v);
    }

    public void reencryptFile(String f, int v) {
        if(!files.containsKey(f)) {
            throw new RuntimeException("Attempting to reencrypt file " + f + " that does not exist.");
        }
        if(files.get(f) > v) {
            throw new RuntimeException("Attempting to reencrypt file " + f + " from " + files.get(f) + " to " + v);
        }
        files.put(f, v);
    }

    public void deleteFile(String f) {
        if(!files.containsKey(f)) {
            throw new RuntimeException("Attempting to delete file " + f + " that does not exist.");
        }
        files.remove(f);
        // TODO: Should we check for remaining keys first?
        keys.remove(f);
    }

    public void addKey(String f, String i, boolean r, boolean w, int v) {
        if(!files.containsKey(f)) {
            throw new RuntimeException("Attempting to add key for file " + f + " that does not exist.");
        }
        if(!keys.containsKey(f)) {
            keys.put(f, new LinkedHashMap<String, CertRing>());
        }
        if(!keys.get(f).containsKey(i)) {
            keys.get(f).put(i, new CertRing(r, w, v));
        } else {
            CertRing kr = keys.get(f).get(i);
            kr.read = kr.read || r;
            kr.write = kr.write || w;
            kr.versions.add(v);
        }
    }

    public void editKeys(String f, String i, boolean r, boolean w) {
        if(!files.containsKey(f)) {
            throw new RuntimeException("Attempting to edit keys for file " + f + " that does not exist.");
        }
        if(!keys.containsKey(f)) {
            throw new RuntimeException("Attempting to edit keys for file " + f + " without keys.");
        }
        if(!keys.get(f).containsKey(i)) {
            throw new RuntimeException("Attempting to edit keys for (" + f + ", " + i + ") that does not exist.");
        }
        CertRing kr = keys.get(f).get(i);
        kr.read = r;
        kr.write = w;
    }

    public void deleteKey(String f, String i, int v) {
        if(!files.containsKey(f)) {
            throw new RuntimeException("Attempting to delete key for file " + f + " that does not exist.");
        }
        if(!keys.containsKey(f)) {
            throw new RuntimeException("Attempting to delete key for file " + f + " without keys.");
        }
        if(!keys.get(f).containsKey(i)) {
            throw new RuntimeException("Attempting to delete key (" + f + ", " + i + ") that does not exist.");
        }
        CertRing kr = keys.get(f).get(i);
        kr.versions.remove(v);
        if(kr.versions.isEmpty()) {
            keys.get(f).remove(i);
        }
    }

    public void addRoleKey(String u, String i) {
        if(!roleKeys.containsKey(u)) {
            if(u.equals(i)) {
                roleKeys.put(u, new HashSet<String>());
            } else {
                throw new RuntimeException("Attempting to add key " + i + " to user " + u + " without self key.");
            }
        }
        roleKeys.get(u).add(i);
    }

    public void deleteRoleKey(String u, String i) {
        if(u.equals(i)) {
            throw new RuntimeException("Attempting to delete self key (" + u + ", " + i + ").");
        }
        if(!roleKeys.containsKey(u) || !roleKeys.get(u).contains(i)) {
            throw new RuntimeException("Attempting to delete key (" + u + ", " + i + ") that does not exist.");
        }
        roleKeys.get(u).remove(i);
    }

    public void deleteUser(String u) {
        if(!roleKeys.containsKey(u)) {
            throw new RuntimeException("Attempting to ban user " + u + " that does not exist.");
        }
        // TODO: do we need to check for leftover keys?
        roleKeys.remove(u);
    }

}

