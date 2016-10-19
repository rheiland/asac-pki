package pki;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import base.Conversion;
import base.Driver;
import base.Measure;
import base.Params;
import base.Simulation;

public class DriverPki extends Driver {
    private Params param;

//    private static final int NUM_RUNS = 100;
//    private static final int NUM_RUNS = 4;
//    private static final int NUM_RUNS = 10;
    private static final int NUM_RUNS = 30;
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

//        param.addBias = 0.7 + (Simulation.rand.nextDouble() * 0.3);
//        param.urBias = 0.3 + (Simulation.rand.nextDouble() * 0.4);
        param.validationCost = 13;

        param.caDepth = Simulation.rand.nextInt(5-2 + 1) + 2;   // random.nextInt(max - min + 1) + min
//        Simulation.caDepth = param.caDepth; 
//        Simulation.caDepth = Simulation.rand.nextInt(5-2 + 1) + 2; 
        java.lang.System.out.println("==================(rwh)DriverPki: param.caDepth = " + param.caDepth);

        //NewPcStarter starter = new NewPcStarter(numrev, numauth);
        param.starter = new StarterPki();
        
        // used in base/Simulation
        param.implementationClassNames = new String[] {
            "pki.ImplementPkiWInCrl",
            "pki.ImplementPkiWInOcsp"
//            "ibe.ImplementRbac0WInPtIbe",
        };

        // X-axis measures
//        param.measures.add("pki.measure.AddBias");

        // 2G measures
        param.measures.add("pki.measure.MemoryCost");
//        param.measures.add("pki.measure.LatencyCost");
        param.measures.add("pki.measure.CADepthCost");

//        param.measures.add("pki.measure.ValidationCost");
//        param.measures.add("pki.measure.RevocationCost");
//        param.measures.add("pki.measure.NumClients");


        param.timelimitPerSimulation = 1 * Conversion.Months;
        param.delta = 1 * Conversion.Hours; // hours

        param.workflowName = "pki.WorkflowNull";
        return param;
    }
}