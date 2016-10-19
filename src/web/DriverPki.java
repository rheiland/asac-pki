package web;

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
    private static final int NUM_RUNS = 1;
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
        param.urBias = 0.3 + (Simulation.rand.nextDouble() * 0.4);

        //NewPcStarter starter = new NewPcStarter(numrev, numauth);
//        param.starter = new StarterIbe();
        param.starter = new StarterOcsp();
        param.implementationClassNames = new String[] {
            "web.ImplementPkiWInOcsp",
        };

        // X-axis measures
        param.measures.add("web.measure.AddBias");
//        param.measures.add("ibe.measure.UrBias");

        // 2G measures
//        param.measures.add("ibe.measure.AsymAuth");
//        param.measures.add("ibe.measure.IbsAuth");

        // 3G measures
//        param.measures.add("ibe.measure.FileRekeyPerUserRevoked");

        param.timelimitPerSimulation = 1 * Conversion.Months;
        param.delta = 1 * Conversion.Hours; // hours

        param.workflowName = "web.WorkflowNull";
        return param;
    }
}