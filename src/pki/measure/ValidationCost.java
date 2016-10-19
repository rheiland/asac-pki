package pki.measure;

//import java.util.List;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
//import base.Scheme;
import base.WorkloadScheme;
import costs.IntSumCost;
import base.SimLogger.Log;


public class ValidationCost extends Measure {
    public ValidationCost() {
        curMeasurement = new IntSumCost();
		java.lang.System.out.println("(rwh)ValidationCost:  " + curMeasurement.getCostStr() );
    }

    @Override
    public String getPrintFriendlyName() {
        return "Validation Cost";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
//        java.lang.System.out.println("(rwh)ValidationCost.postExecMeasurement:  ----------");
        // Get the workload action
        Property p = WorkloadScheme.actionProperty;
        Action a = (Action) w.getCurMeasure(p);
        if(a != null) {
//            if(a.name.equals("checkDateTimeValid")) {
            if(a.name.equals("checkPathValidation")) {
                curMeasurement.aggregate(1);
                Log.v("(rwh)ValidationCost(): DateTime:  " + curMeasurement.getCostStr());
//                java.lang.System.out.println("(rwh)ValidationCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
            }
            else if(a.name.equals("checkPathLength")) {
                curMeasurement.aggregate(2);
                Log.v("(rwh)ValidationCost(): PathLength:  " + curMeasurement.getCostStr());
//                java.lang.System.out.println("(rwh)ValidationCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
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
//		                java.lang.System.out.println("(rwh)ValidationCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
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