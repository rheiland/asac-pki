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

public class ActorAdmin extends ActorMachine {
    double addBias = 0.5;
    double urBias = 0.5;

    public static final ActorProperty sentinelProperty = new ActorProperty(ActorAdmin.class, Q_TIME);


    public final ActorProperty timeProperty = new ActorProperty(this, Q_TIME);
    public final ActorProperty addBiasProperty = new ActorProperty(this, "addBias");
    public final ActorProperty urBiasProperty = new ActorProperty(this, "urBias");

    Set<Property> measurables = new HashSet<Property>();

    public ActorAdmin() {
        measurables.add(timeProperty);
        measurables.add(addBiasProperty);
        measurables.add(urBiasProperty);
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
        if(p.name.equals("addBias")) return true;
        if(p.name.equals("urBias")) return true;
        return false;
    }

    @Override
    public Object getCurMeasure(Property p) {
        if(!sentinelProperty.matchQualifier(p)) {
            throw new InvalidMeasureException(p);
        } else if(p.name.equals(Q_TIME)) {
            return 0;
        } else if(p.name.equals("addBias")) {
            return addBias;
        } else if(p.name.equals("urBias")) {
            return urBias;
        } else {
            throw new InvalidMeasureException(p);
        }
    }

    @Override
    public Action nextAction(Params params) {
        double thresholdManageU = 0;
        double thresholdManageR = 0;
        double thresholdManageP = 0;
        // thresholdManageUr and thresholdManagePa should initially be equal (each equal to their
        // sum). Balance between them is done below using urBias
//        double thresholdManageUr = 0.1 * Math.sqrt(((ibe.Rbac0WState)sys.impl.wScheme.state).U.size())
//                / (1 * Conversion.Days);
//        double thresholdManagePa = thresholdManageUr;

//        if(thresholdManageUr > 1.0) {
//            throw new RuntimeException("Timestep is overflowing, total admin rate is "
//                    + thresholdManageUr);
//        } else if(thresholdManageUr > 0.5) {
//            Log.w("Timestep may be overflowing, total admin rate is " + thresholdManageUr);
//        }

        addBias = params.addBias;
//        urBias = params.urBias;
//
//        thresholdManageUr *= urBias;
//        thresholdManagePa *= (1 - urBias);

//        double coin = Simulation.rand.nextDouble() / params.delta;
//        Log.d("Admin coin: " + coin + ". Thresholds: " +
//                thresholdManageU + " " +
//                thresholdManageR + " " +
//                thresholdManageP + " " +
//                thresholdManageUr + " " +
//                thresholdManagePa + " " +
//                addBias + " " +
//                urBias
//        );

        // ... more in ibe/ActorAdmin

        return null;
    }

    @Override
    public String getPrefix() {
        return prefix();
    }

    public static String prefix() {
        return "a";
    }
}

