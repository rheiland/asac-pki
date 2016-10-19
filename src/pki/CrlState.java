package pki;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import base.InvalidMeasureException;
import base.Property;
import base.State;

public class CrlState extends State {
    public static final Property sizeofProperty = new Property(CrlState.class, "sizeof");


    @Override
    public Set<Property> getMeasurables() {
        HashSet<Property> ret = new HashSet<Property>();
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
        CrlState ret = new CrlState();

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

}