package measures;

import costs.UnsignedIntMaxCost;
import base.State;
import base.Cost;
import base.Measurable;
import base.Measure;
import base.Property;
import base.WorkloadState;

public class MaxWStateSizeMeasure extends Measure {
	public MaxWStateSizeMeasure() {
		curMeasurement = new UnsignedIntMaxCost();
	}

	@Override
	public String getMeasureName() {
		return "Maximum_Workload_Size";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
	}

	@Override
	public void postExecMeasurement(Measurable w) {
		String[] params = {"ALL"};
		Property p = new Property(WorkloadState.class, "sizeof", params);
		Integer size = (Integer) w.getCurMeasure(p);
		Cost<Integer> measure = (Cost<Integer>) curMeasurement;
		measure.aggregate(size);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		String[] params = {"ALL"};
		Property p = new Property(WorkloadState.class, "sizeof", params);
		return w.isMeasurable(p);
	}

	@Override
	public String getPrintFriendlyName() {
		return "Workload Size(Max)";
	}
}
