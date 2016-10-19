package measures;

import costs.IntReplaceCost;
import base.Measurable;
import base.Measure;
import base.Property;


public class NumberOfObjects extends Measure {

	public NumberOfObjects() {
		curMeasurement = new IntReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Number_Of_Objects";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Number of Objects";
	}

	@Override
	public void preExecMeasurement(Measurable w) {

	}

	@Override
	public void postExecMeasurement(Measurable w) {
		String[] param = {"O"};
		Property p = new Property(base.WorkloadState.class, "sizeof", param);
		Integer size = (Integer) w.getCurMeasure(p);
		curMeasurement.aggregate(size);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return w.isMeasurable(new Property(base.WorkloadState.class, "O", new String[0]));
	}

}
