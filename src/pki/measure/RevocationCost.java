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


public class RevocationCost extends Measure {
    public RevocationCost() {
        curMeasurement = new IntSumCost();
		java.lang.System.out.println("(rwh)RevocationCost:  " + curMeasurement.getCostStr() );
    }

    @Override
    public String getPrintFriendlyName() {
        return "Revocation Cost";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
        java.lang.System.out.println("(rwh)RevocationCost.postExecMeasurement:  ----------");
        // Get the workload action
        Property p = WorkloadScheme.actionProperty;
        Action a = (Action) w.getCurMeasure(p);
        if(a != null) {
            java.lang.System.out.println("(rwh)RevocationCost():  a.name=" + a.name);
//            if(a.name.equals("checkRevocation")) {
            if(a.name.equals("checkPathValidation")) {
                curMeasurement.aggregate(3);
                Log.v("(rwh)RevocationCost():   " + curMeasurement.getCostStr());
//                java.lang.System.out.println("(rwh)RevocationCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
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
//		                java.lang.System.out.println("(rwh)RevocationCost.postExecMeasurement:  " + curMeasurement.getCostStr() );
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