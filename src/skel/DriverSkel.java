package skel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import base.Conversion;
import base.Driver;
import base.Measure;
import base.Params;
import base.Simulation;

public class DriverSkel extends Driver {
    private Params param;

//    private static final int NUM_RUNS = 100;
    private static final int NUM_RUNS = 2;
    @Override
    public int getNumRuns() {
        return NUM_RUNS;
    }

    @Override
    public String generatePlotter() {
        if(param == null)
            return null;
        List<Measure> plots = new ArrayList<Measure>();
        try {
            for(String mname : param.measures) {
                plots.add((Measure)Class.forName(mname).newInstance());
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        Collections.sort(plots);

        return super.getPlotter(param.implementationClassNames, plots);
    }

    @Override
    public Params generateSimulation() {
        param = new Params();

        param.addBias = 0.7 + (Simulation.rand.nextDouble() * 0.3);
//        param.urBias = 0.3 + (Simulation.rand.nextDouble() * 0.4);

        //NewPcStarter starter = new NewPcStarter(numrev, numauth);
        param.starter = new StarterSkel();
        param.implementationClassNames = new String[] {
            "skel.ImplementSkelWInModel1"
//            "ibe.ImplementRbac0WInPtIbe",
//            "ibe.ImplementRbac0WInPubKey",
//            "ibe.ImplementRbac0WInSymKey",
//            "ibe.ImplementRbac0WInSymKeyDel"
        };

        // X-axis measures
//        param.measures.add("skel.measure.AddBias");
//        param.measures.add("ibe.measure.UrBias");

        // 2G measures
        param.measures.add("skel.measure.Cost1");
//        param.measures.add("ibe.measure.AsymAuth");
//        param.measures.add("ibe.measure.AsymDecCl");
//        param.measures.add("ibe.measure.AsymDecSu");
//        param.measures.add("ibe.measure.AsymEncCl");
//        param.measures.add("ibe.measure.AsymEncSu");
//        param.measures.add("ibe.measure.AsymGenCl");
//        param.measures.add("ibe.measure.AsymGenSu");
//        param.measures.add("ibe.measure.FileClToRm");
//        param.measures.add("ibe.measure.FileClToSt");
//        param.measures.add("ibe.measure.FileRmToCl");
//        param.measures.add("ibe.measure.FileRmToSt");
//        param.measures.add("ibe.measure.FileStToCl");
//        param.measures.add("ibe.measure.FileStToRm");
//        param.measures.add("ibe.measure.IbeDecCl");
//        param.measures.add("ibe.measure.IbeDecSu");
//        param.measures.add("ibe.measure.IbeEncCl");
//        param.measures.add("ibe.measure.IbeEncSu");
//        param.measures.add("ibe.measure.IbeGenSu");
//        param.measures.add("ibe.measure.IbsAuth");
//        param.measures.add("ibe.measure.KeyClToSt");
//        param.measures.add("ibe.measure.KeyRmToCl");
//        param.measures.add("ibe.measure.KeyStToCl");
//        param.measures.add("ibe.measure.KeyStToSu");
//        param.measures.add("ibe.measure.KeySuToSt");
//        param.measures.add("ibe.measure.SymDecCl");
//        param.measures.add("ibe.measure.SymDecRm");
//        param.measures.add("ibe.measure.SymEncCl");
//        param.measures.add("ibe.measure.SymEncRm");
//        param.measures.add("ibe.measure.SymGenCl");
//        param.measures.add("ibe.measure.SymGenRm");
//        param.measures.add("ibe.measure.SymGenSu");

        // 3G measures
//        param.measures.add("ibe.measure.IbeEncRevU");
//        param.measures.add("ibe.measure.IbeEncPerUserRevoked");
//        param.measures.add("ibe.measure.IbeEncRevP");
//        param.measures.add("ibe.measure.IbeEncPerPermRevoked");
//        param.measures.add("ibe.measure.FileRekeyRevU");
//        param.measures.add("ibe.measure.FileRekeyPerUserRevoked");
//        param.measures.add("ibe.measure.FileRekeyRevP");
//        param.measures.add("ibe.measure.FileRekeyPerPermRevoked");

        param.timelimitPerSimulation = 1 * Conversion.Months;
        param.delta = 1 * Conversion.Hours; // hours

        param.workflowName = "skel.WorkflowNull";

        return param;
    }
}