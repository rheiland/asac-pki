package pki.measure;
import java.util.Set;
//import java.util.List;

import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
//import base.Scheme;
import base.WorkloadScheme;
import base.ActorMachine;
import base.SimLogger.Log;
import costs.IntReplaceCost;

public class NumClients extends Measure {
    public NumClients() {
        curMeasurement = new IntReplaceCost();
		java.lang.System.out.println("(rwh)NumClients:  " + curMeasurement.getCostStr() );
    }

    @Override
    public String getPrintFriendlyName() {
        return "Num Clients";
    }

    @Override
    public void postExecMeasurement(Measurable w) {
        java.lang.System.out.println("(rwh)NumClients.postExecMeasurement:  ----------");
		Property p = new Property(ActorMachine.class, "GetNames", new String[0]);
		Set<String> names = (Set<String>) w.getCurMeasure(p);
		int numClients = 0;
		for(String n : names) {
			if(n.startsWith("c")) {
				numClients++;
			}
		}
		curMeasurement.aggregate(numClients);
    }
    
//    public void postExecMeasurement(Measurable w) {
//        java.lang.System.out.println("(rwh)NumClients.postExecMeasurement:  ----------");
//        // Get the workload action
//        Property p = WorkloadScheme.actionProperty;
//        Action a = (Action) w.getCurMeasure(p);
//        if(a != null) {
//            java.lang.System.out.println("(rwh)NumClients():  a.name=" + a.name);
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