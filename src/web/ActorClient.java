package web;

import java.util.HashSet;
import java.util.Set;

import base.Action;
import base.ActorMachine;
import base.ActorProperty;
import base.Conversion;
import base.InvalidMeasureException;
import base.Params;
import base.Property;
import base.Simulation;

public class ActorClient extends ActorMachine {
    public static final ActorProperty sentinelProperty = new ActorProperty(ActorClient.class, Q_TIME);

    public final ActorProperty timeProperty = new ActorProperty(this, Q_TIME);

    Set<Property> measurables = new HashSet<Property>();

    public ActorClient() {
        measurables.add(timeProperty);
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
        return false;
    }

    @Override
    public Object getCurMeasure(Property p) {
        if(!sentinelProperty.matchQualifier(p)) {
            throw new InvalidMeasureException(p);
        }
        if(p.name.equals(Q_TIME)) {
            return 0;
        }
        throw new InvalidMeasureException(p);
    }

    @Override
    public Action nextAction(Params params) {
        double thresholdAuth = 1.0 / (1 * Conversion.Days);
        double thresholdAddP = 1.0 / (5 * Conversion.Days);

        double coin = Simulation.rand.nextDouble() / params.delta;

        if(coin <= thresholdAuth) {
            return new Action(true, "auth", actor, actor, null);
        }
        coin -= thresholdAuth;

        if(coin <= thresholdAddP) {
            // add permission
            // Let addParameters choose which one
            return new Action("addP", actor, null);
        }
        coin -= thresholdAddP;

        return null;
    }

    @Override
    public String getPrefix() {
        return "u";
    }

}

