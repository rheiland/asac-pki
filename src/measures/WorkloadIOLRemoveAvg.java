package measures;

import costs.DoubleReplaceCost;
import base.Measurable;
import base.Measure;

public class WorkloadIOLRemoveAvg extends Measure {
	Measure io = new WorkloadIOLRemove();
	Measure instances = new InstancesLRemove();

	public WorkloadIOLRemoveAvg() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Workload_IO_LRemove_Avg";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Average workload I/O for LRemove operations";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		io.preExecMeasurement(w);
		instances.preExecMeasurement(w);
	}

	@Override
	public void postExecMeasurement(Measurable w) {
		io.postExecMeasurement(w);
		instances.postExecMeasurement(w);
		
		int ioCur = (Integer)io.getCurCost();
		double instCur = (Integer)instances.getCurCost();
		
		curMeasurement.aggregate(ioCur / instCur);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return io.isMeasurementvalid(w) && instances.isMeasurementvalid(w);
	}

}
