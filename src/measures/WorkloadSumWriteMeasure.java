package measures;

import costs.IntSumCost;
import base.Measurable;
import base.Measure;
import base.Property;
import base.WorkloadState;

public class WorkloadSumWriteMeasure extends Measure {

	public WorkloadSumWriteMeasure() {
		curMeasurement = new IntSumCost();
	}

	@Override
	public String getMeasureName() {
		return "Sum_Writes";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Sum Writes";
	}

	@Override
	public void preExecMeasurement(Measurable w) {

	}

	@Override
	public void postExecMeasurement(Measurable w) {
		Property in = new Property(WorkloadState.class, WorkloadState.Q_ICOST, new String[0]);
		Integer numIn = (Integer) w.getCurMeasure(in);
		curMeasurement.aggregate(numIn);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return w.isMeasurable(new Property(WorkloadState.class, WorkloadState.Q_ICOST, new String[0]));
	}

}
