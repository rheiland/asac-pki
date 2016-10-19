package pki.measure;

import java.util.Set;
import java.util.List;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.Scheme;
import base.WorkloadScheme;
import base.ActorMachine;
import base.SimLogger.Log;
import base.Implementation;
import base.Simulation;

import costs.IntReplaceCost;

public class CADepthCost extends Measure {
    public CADepthCost() {
        curMeasurement = new IntReplaceCost();
		java.lang.System.out.println("(rwh)CADepthCost:  " + curMeasurement.getCostStr() );
    }

    @Override
    public String getPrintFriendlyName() {
        return "CA Depth";
    }

    @Override
    public void postExecMeasurement(Measurable w) {

    	//rwh: rf.  public class IbeEncSu extends Measure
        // Get the actions executed
//    	java.lang.System.out.println("(rwh)CADepthCost.postExecMeasurement: randnum= " + Simulation.rand.nextInt(5-2 + 1) + 2);   // random.nextInt(max - min + 1) + min
//    	java.lang.System.out.println("(rwh)CADepthCost.postExecMeasurement: randnum= " + Simulation.rand.nextInt(5-2 + 1) + 2);   // random.nextInt(max - min + 1) + min
//    	java.lang.System.out.println("(rwh)CADepthCost.postExecMeasurement: Sim.caDepth= " + Simulation.caDepth);   // random.nextInt(max - min + 1) + min
//    	java.lang.System.out.println("(rwh)CADepthCost.postExecMeasurement: w.sys.rand.nextInt(5)= " + w.sys.rand.nextInt(5));
        Property pActions = Scheme.actionsProperty;
        List<Action> actions = (List<Action>) w.getCurMeasure(pActions);
        
        Property p = WorkloadScheme.actionProperty;
        Action a = (Action) w.getCurMeasure(p);
        
		int caDepth = Integer.parseInt(a.params[1]);
//		java.lang.System.out.println("caDepthCost: caDepth="+caDepth);
		curMeasurement.aggregate(caDepth);	
		
//		java.lang.System.out.println("(rwh)CADepthCost:  got List of actions");
//        for (Action a:actions) 
//          java.lang.System.out.println("(rwh)CADepthCost.postExecMeasurement:  ---------- action=" + a);
//          java.lang.System.out.println("(rwh)CADepthCost.postExecMeasurement:  ---------- caDepth=" + sys.caDepth);  //rwh: how do I access param.caDepth?
        
//        java.lang.System.out.println("(rwh)CADepthCost.postExecMeasurement:  ---------- caDepth=" + sys.caDepth);  //rwh: how do I access param.caDepth?
//		curMeasurement.aggregate(sys.caDepth);
    	// Simulation.rand.nextInt(5-2 + 1) + 2;   // random.nextInt(max - min + 1) + min
    }
    
//    public void postExecMeasurement(Measurable w) {
//        java.lang.System.out.println("(rwh)CADepthCost.postExecMeasurement:  ----------");
//        // Get the workload action
//        Property p = WorkloadScheme.actionProperty;
//        Action a = (Action) w.getCurMeasure(p);
//        if(a != null) {
//            java.lang.System.out.println("(rwh)CADepthCost():  a.name=" + a.name);
////            if(a.name.equals("checkRevocation")) {
//            if(a.name.equals("checkPathValidation")) {
//                curMeasurement.aggregate(3);
//                Log.v("(rwh)RevocationCost():   " + curMeasurement.getCostStr());
////                java.lang.System.out.println("(rwh)RevocationCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
//            }
//        }
//    }
    
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
//		                java.lang.System.out.println("(rwh)RevocationCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
//                    }
////               }
//            }
//        }
//    }

    @Override
    public boolean isMeasurementvalid(Measurable w) {
//        Property p = Scheme.actionsProperty;
//        Property p = WorkloadScheme.actionProperty;
//        return w.isMeasurable(p);
		Property p = new Property(ActorMachine.class, "GetNames", new String[0]);
		return w.isMeasurable(p);
    }
}