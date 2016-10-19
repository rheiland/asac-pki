package pki.measure;

//import java.util.List;
import java.lang.reflect.Method;

import base.Action;

import base.Measurable;
import base.Measure;
import base.Property;
//import base.Scheme;
import base.WorkloadScheme;
import base.Simulation;

//import costs.IntSumCost;
import costs.IntReplaceCost;
import base.SimLogger.Log;


public class MemoryCost extends Measure {
    public MemoryCost() {
//        curMeasurement = new IntSumCost();
        curMeasurement = new IntReplaceCost();
		java.lang.System.out.println("-------------(rwh)MemoryCost constr:  " + curMeasurement.getCostStr() );
    }

    @Override
    public String getPrintFriendlyName() {
        return "Revocation Response Size";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
//        java.lang.System.out.println("(rwh)MemoryCost.postExecMeasurement:  ----------");
    	String type = Util.identify(w);
//        java.lang.System.out.println("(rwh)MemoryCost.postExecMeasurement:  type=" + type );
    	
        // Get the workload action
        Property p = WorkloadScheme.actionProperty;
        Action a = (Action) w.getCurMeasure(p);
        
//		java.lang.System.out.println("MemoryCost: ---------- Action a");

        // to dump all public methods of Action
//		Class tClass = a.getClass();
//		Method[] methods = tClass.getMethods();

//		for (int i = 0; i < methods.length; i++) {
//			java.lang.System.out.println("  public method: " + methods[i]);
//		}


//		java.lang.System.out.println("  a.params.length= " + a.params.length);
//		for (int i = 0; i < a.params.length; i++) 
//			java.lang.System.out.println("  params[i]: i,param=: "+ a.params[i]);

		// alternatively, could do:
//		for(String param : a.params){
//			java.lang.System.out.println("  param=: "+ param);
//		}

		int caDepth = Integer.parseInt(a.params[1]);
//		java.lang.System.out.println("MemoryCost: caDepth="+caDepth);
		
        if(a != null) {
//            if(a.name.equals("checkDateTimeValid")) {
            if(a.name.equals("checkPathValidation")) {
              if(type.equals("CRL")) {  // more memory
//            	  int kilobytes = caDepth*100 + Simulation.rand.nextInt(50-20 + 1) + 20 ;  // random.nextInt(max - min + 1) + min
            	  int kilobytes = caDepth* (Simulation.rand.nextInt(1000-50 + 1) + 50);  // random.nextInt(max - min + 1) + min
                 curMeasurement.aggregate(kilobytes);
              }
              else if(type.equals("OCSP")) { // less memory
//            	  int kilobytes = caDepth*20 + Simulation.rand.nextInt(10-5 + 1) + 5;  // random.nextInt(max - min + 1) + min
            	  int kilobytes = caDepth* (Simulation.rand.nextInt(100-20 + 1) + 20);  // random.nextInt(max - min + 1) + min
                 curMeasurement.aggregate(kilobytes);
//                Log.v("(rwh)MemoryCost(): DateTime:  " + curMeasurement.getCostStr());
//                java.lang.System.out.println("(rwh)MemoryCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
              }
            }
//            else if(a.name.equals("checkPathLength")) {
//                curMeasurement.aggregate(2);
//                Log.v("(rwh)MemoryCost(): PathLength:  " + curMeasurement.getCostStr());
////                java.lang.System.out.println("(rwh)MemoryCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
//            }
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
//		                java.lang.System.out.println("(rwh)MemoryCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
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