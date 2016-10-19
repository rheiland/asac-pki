package measures;

import java.util.Iterator;
import java.util.Set;

import base.Action;
import base.ActorMachine;
import base.ActorProperty;
import base.Cost;
import base.Measurable;
import base.Measure;
import base.Property;
import base.System;
import costs.IntSumCost;

public class SumTimeMeasure extends Measure {


	public SumTimeMeasure() {
		curMeasurement = new IntSumCost();
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		Action curAction = (Action) w.getCurMeasure(new Property(System.class, System.Q_CURACTION, new String[0]));
		if(curAction == null) return;
		Set<String> names = (Set<String>) w.getCurMeasure(new Property(ActorMachine.class, "GetNames", new String[0]));
		Iterator<String> it = names.iterator();
		if(it.hasNext()) {
			String n = it.next();
			String param[] = {curAction.name, n};
			Integer time = (Integer) w.getCurMeasure(new ActorProperty(ActorMachine.class, ActorMachine.Q_TIME, param, curAction.name));
			Cost<Integer> measure = (Cost<Integer>) curMeasurement;
			measure.aggregate(time);
		}
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
		return "Total_Actor_Time_Spent";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Total Actor Time";
	}

}
