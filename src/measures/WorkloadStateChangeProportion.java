package measures;

import costs.DoubleReplaceCost;
import base.Measurable;
import base.Measure;

public class WorkloadStateChangeProportion extends Measure {

	Measure writes = new WorkloadSumWriteMeasure();
	Measure sSize = new SumWStateSizeMeasure();

	public WorkloadStateChangeProportion() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Proportion_Workload_State_Change";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Proportion of Workload State Change";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		writes.preExecMeasurement(w);
		sSize.preExecMeasurement(w);
	}

	@Override
	public void postExecMeasurement(Measurable w) {
		writes.postExecMeasurement(w);
		sSize.postExecMeasurement(w);

		Integer numIO = (Integer) writes.getCurCost();
		Integer numSize = (Integer) sSize.getCurCost();

		Double ratio = new Double((double) numIO / numSize);
		curMeasurement.aggregate(ratio);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return writes.isMeasurementvalid(w) && sSize.isMeasurementvalid(w);
	}

}
