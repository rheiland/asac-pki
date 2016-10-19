package measures;

import java.util.Iterator;
import java.util.Set;

import costs.IntReplaceCost;
import base.*;
import base.System;
import base.ActorProperty;

/**
 * This measure represents the time during which the actor is occupied while executing this action.
 * This is a special measure in that EVERY workload scheme must implement this.
 * @author yechen
 *
 */
public class TimeMeasure extends Measure {


	public TimeMeasure() {
		curMeasurement = new IntReplaceCost();
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		Action curAction = (Action) w.getCurMeasure(new Property(System.class, System.Q_CURACTION, new String[0]));
		if(curAction == null) return;
		ActorMachine m = (ActorMachine) w.getCurMeasure(new Property(ActorMachine.class, "CurActor", new String[0]));
		Cost<Integer> measure = (Cost<Integer>) curMeasurement;
		String[] param = new String[curAction.params.length];
		java.lang.System.arraycopy(curAction.params, 0, param, 0, curAction.params.length);
		Integer time = (Integer) m.getCurMeasure(new ActorProperty(ActorMachine.class, ActorMachine.Q_TIME, param, curAction.name));
		measure.aggregate(time);
	}

	@Override
	public void postExecMeasurement(Measurable w) {

	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return w.isMeasurable(new ActorProperty(ActorMachine.class, ActorMachine.Q_TIME));
	}

	@Override
	public String getMeasureName() {
		return "Actor_Time_Spent";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Last Action Time";
	}



}
