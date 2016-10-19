package web;

import base.SimulationStarter;
import base.SimLogger.Log;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class StarterOcsp extends SimulationStarter implements Serializable {
    private static final long serialVersionUID = 0xFA760A7L;

//    private static final String INIT = "firewall1";

    public StarterOcsp() {}

    @Override
    public PkiWState build() {
        PkiWState ret = new PkiWState();
        ret.addUser("a0");  // a single Admin ?

//        List<String> ur;
//        List<String> pa;
//
//        try {
//            ur = Files.readAllLines(Paths.get("init/" + INIT + ".ur.ass"), Charset.defaultCharset());
//            pa = Files.readAllLines(Paths.get("init/" + INIT + ".pa.ass"), Charset.defaultCharset());
//        } catch(IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        int users = ur.get(1).length() - ur.get(1).replace(",", "").length() + 1;
//        int permissions = pa.get(2).length() - pa.get(2).replace(",", "").length() + 1;
//        int roles = ur.get(2).length() - ur.get(2).replace(",", "").length() + 1;
//        Log.d("Initial state will have " +
//                users + " users, " +
//                permissions + " permissions, " +
//                roles + " roles.");
//
//        if(roles != pa.get(1).length() - pa.get(1).replace(",", "").length() + 1) {
//            throw new RuntimeException("Role count mismatch");
//        }
//        for(int r = 0; r < roles; r++) {
//            Log.v("Adding r" + r + " to initial state");
//            ret.addRole("r" + r);
//        }
//        for(int u = 0; u < users; u++) {
//            Log.v("Adding u" + u + " to initial state");
//            ret.addUser("u" + u);
//            for(int r = 0; r < roles; r++) {
//                if(ur.get(4+u).charAt(2*r) == '1') {
//                    Log.v("Adding (u" + u + ",r" + r + ") to initial state");
//                    ret.assignUser("u" + u, "r" + r);
//                }
//            }
//        }
//        for(int p = 0; p < permissions; p++) {
//            Log.v("Adding p" + p + " to initial state");
//            ret.addPermission("p" + p);
//            for(int r = 0; r < roles; r++) {
//                if(pa.get(4+r).charAt(2*p) == '1') {
//                    Log.v("Adding (p" + p + ",r" + r + ") to initial state");
//                    ret.assignPermission("p" + p, "r" + r);
//                }
//            }
//        }

        return ret;
    }

    @Override
    public HashMap<String, Integer> buildActorMachine() {
        HashMap<String, Integer> ret = new HashMap<String, Integer>();
        ret.put("web.ActorAdmin", 1);

//        List<String> ur;
//        try {
//            ur = Files.readAllLines(Paths.get("init/" + INIT + ".ur.ass"), Charset.defaultCharset());
//        } catch(IOException e) {
//            throw new RuntimeException(e);
//        }
//        int users = ur.get(1).length() - ur.get(1).replace(",", "").length() + 1;

//        ret.put("web.ActorUser", users);
        ret.put("web.ActorUser", 5);
        return ret;
    }
}