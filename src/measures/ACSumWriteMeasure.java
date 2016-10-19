package measures;

import base.Measurable;
import base.Measure;
import base.Property;
import base.State;
import costs.IntReplaceCost;
import costs.IntSumCost;

public class ACSumWriteMeasure extends Measure{
	
	public ACSumWriteMeasure() {
		curMeasurement = new IntSumCost();
	}

	@Override
	public String getMeasureName() {
		return "Sum_AC_Writes_Measure";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Sum AC Writes Measure";
	}

	@Override
	public void preExecMeasurement(Measurable w) {

	}

	@Override
	public void postExecMeasurement(Measurable w) {
		Property in = new Property(State.class, State.Q_ICOST, new String[0]);
		Integer numIn = (Integer) w.getCurMeasure(in);
		curMeasurement.aggregate(numIn);

	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return w.isMeasurable(new Property(State.class, State.Q_ICOST, new String[0]));
	}
}
