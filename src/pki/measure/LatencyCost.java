package pki.measure;

//import java.util.List;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
//import base.Scheme;
import base.WorkloadScheme;
import base.Simulation;

//import costs.IntSumCost;
//import costs.DoubleSumCost;
import costs.DoubleReplaceCost;
//import costs.UnsignedDoubleMaxCost;
import base.SimLogger.Log;

public class LatencyCost extends Measure {

    public LatencyCost() {
//        curMeasurement = new DoubleSumCost();
//        curMeasurement = new UnsignedDoubleMaxCost();
        curMeasurement = new DoubleReplaceCost();
		java.lang.System.out.println("(rwh)LatencyCost:  " + curMeasurement.getCostStr() );
    }

    @Override
    public String getPrintFriendlyName() {
        return "Latency Cost";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
//        java.lang.System.out.println("(rwh)LatencyCost.postExecMeasurement:  ----------");
    	String type = Util.identify(w);
//        java.lang.System.out.println("(rwh)LatencyCost.postExecMeasurement:  type=" + type );
    	
        // Get the workload action
        Property p = WorkloadScheme.actionProperty;
        Action a = (Action) w.getCurMeasure(p);
        if(a != null) {
            if(a.name.equals("checkPathValidation")) {
                if(type.equals("CRL"))   // more latency
                    curMeasurement.aggregate(2.2 + Simulation.rand.nextDouble() * 8.0);
                 else if(type.equals("OCSP"))  // less latency
                    curMeasurement.aggregate(1.1 + Simulation.rand.nextDouble() * 4.0);
//                Log.v("(rwh)LatencyCost(): DateTime:  " + curMeasurement.getCostStr());
//                java.lang.System.out.println("(rwh)LatencyCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
            }
            else if(a.name.equals("checkPathLength")) {
                curMeasurement.aggregate(2);
//                Log.v("(rwh)LatencyCost(): PathLength:  " + curMeasurement.getCostStr());
//                java.lang.System.out.println("(rwh)LatencyCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
            }
        }
    }
    
//    public void postExecMeasurement(Measurable w) {
//        String type = Util.identify(w);
//
//        // Get the actions executed
//        Property pActions = Scheme.actionsProperty;
//        List<Action> actions = (List<Action>) w.getCurMeasure(pActions);
//
//        if(actions != null && actions.size() > 0) {
//            for(Action a: actions) {
////                if(type.equals("PT")) {
////                    if(a.name.equals("reencryptFile")) {
//                    if(a.name.equals("checkDateTimeValid")) {
//                        curMeasurement.aggregate(1);
//		                java.lang.System.out.println("(rwh)LatencyCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
//                    }
////               }
//            }
//        }
//    }

    @Override
    public boolean isMeasurementvalid(Measurable w) {
//        Property p = Scheme.actionsProperty;
        Property p = WorkloadScheme.actionProperty;
        return w.isMeasurable(p);
    }
}