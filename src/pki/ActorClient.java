package pki;

import java.util.HashSet;
import java.util.Set;

import base.Action;
import base.ActorMachine;
import base.ActorProperty;
import base.Conversion;
import base.InvalidMeasureException;
import base.Params;
import base.Property;
import base.SimLogger.Log;
import base.Simulation;

public class ActorClient extends ActorMachine {
    double addBias = 0.5;
    double cost1 = 0.5;
//    double urBias = 0.5;

    public static final ActorProperty sentinelProperty = new ActorProperty(ActorClient.class, Q_TIME);

    public final ActorProperty timeProperty = new ActorProperty(this, Q_TIME);
    public final ActorProperty addBiasProperty = new ActorProperty(this, "addBias");
    public final ActorProperty cost1Property = new ActorProperty(this, "cost1");
//    public final ActorProperty urBiasProperty = new ActorProperty(this, "urBias");

    Set<Property> measurables = new HashSet<Property>();

    public ActorClient() {
        measurables.add(cost1Property);
        measurables.add(timeProperty);
//        measurables.add(addBiasProperty);
//        measurables.add(urBiasProperty);
    }

    public Set<Property> getMeasurables() {
        return measurables;
    }

    @Override
    public boolean isMeasurable(Property p) {
        if(!sentinelProperty.matchQualifier(p)) {
            return false;
        }
        if(p.name.equals(Q_TIME)) return true;
        if(p.name.equals("cost1")) {
            java.lang.System.out.println("(rwh)ActorClient.isMeasurable:  p.name.equals cost1");
        	return true;
        }
//        if(p.name.equals("addBias")) return true;
//        if(p.name.equals("urBias")) return true;
        return false;
    }

    @Override
    public Object getCurMeasure(Property p) {
        if(!sentinelProperty.matchQualifier(p)) {
            throw new InvalidMeasureException(p);
        } else if(p.name.equals(Q_TIME)) {
            return 0;
        } else if(p.name.equals("cost1")) {
            java.lang.System.out.println("(rwh)ActorClient.getCurMeasure:  cost1=" + cost1 );
            return cost1;
//        } else if(p.name.equals("addBias")) {
//            return addBias;
//        } else if(p.name.equals("urBias")) {
//            return urBias;
        } else {
            throw new InvalidMeasureException(p);
        }
    }

    @Override
    public Action nextAction(Params params) {
//        double coin = Simulation.rand.nextDouble() / params.delta;
        double coin = Simulation.rand.nextDouble();
        coin = 0.0;  // rwh: don't use randomness
        Log.d("Client coin: " + coin );
        
//        Log.d("Admin coin: " + coin + ". Thresholds: " +
//                thresholdManageU + " " +
//                thresholdManageR + " " +
//                thresholdManageP + " " +
//                thresholdManageUr + " " +
//                thresholdManagePa + " " +
//                addBias + " " +
//                urBias
//        );

        // (rwh) Let's say ~25% of sites use https (https://www.wired.com/2016/03/https-adoption-google-report/)
        if(coin <= 0.25) {
//            return new Action("checkPathValidation", actor, null, null);
        	int caDepth = Simulation.rand.nextInt(5-2 + 1) + 2;
            return new Action("checkPathValidation", actor, Integer.toString(caDepth), null);
//            return new Action("checkPathValidation", actor, null, null);

//            return new Action("checkDateTimeValid", actor, null, null);
//            return new Action("checkPathLength", actor, null, null);
        }

        return null;
    }

    @Override
    public String getPrefix() {
        return prefix();
    }

    public static String prefix() {
        return "c";
    }
}