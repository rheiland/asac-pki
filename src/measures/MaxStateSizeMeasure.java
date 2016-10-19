package measures;

import costs.IntMaxCost;
import costs.UnsignedIntMaxCost;
import base.Scheme;
import base.State;
import base.Cost;
import base.Implementation;
import base.Measurable;
import base.Measure;
import base.Property;

public class MaxStateSizeMeasure extends Measure {

	public MaxStateSizeMeasure() {
		curMeasurement = new UnsignedIntMaxCost();
	}

	@Override
	public String getMeasureName() {
		return "Maximum_State_Size";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
	}

	@Override
	public void postExecMeasurement(Measurable w) {
		String[] params = {"ALL"};
		Property p = new Property(State.class, "sizeof", params);
		Integer size = (Integer) w.getCurMeasure(p);
		Cost<Integer> measure = (Cost<Integer>) curMeasurement;
		measure.aggregate(size);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		String[] params = {"ALL"};
		Property p = new Property(State.class, "sizeof", params);
		return w.isMeasurable(p);
	}

	@Override
	public String getPrintFriendlyName() {
		return "Maximum State Size in Simulated Scheme";
	}

}
