package measures;

import costs.IntSumCost;
import base.Scheme;
import base.State;
import base.Cost;
import base.Implementation;
import base.Measurable;
import base.Measure;
import base.Property;

public class SumStateSizeMeasure extends Measure {

	public SumStateSizeMeasure() {
		curMeasurement = new IntSumCost();
	}

	@Override
	public String getMeasureName() {
		return "Summation_State_Size";
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
		return "Sum of State Size in Simulated Scheme";
	}

}
