package measures;

import costs.DoubleReplaceCost;
import base.Measurable;
import base.Measure;

public class WorkloadIOLJoinAvg extends Measure {
	Measure io = new WorkloadIOLJoin();
	Measure instances = new InstancesLJoin();

	public WorkloadIOLJoinAvg() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Workload_IO_LJoin_Avg";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Average workload I/O for LJoin operations";
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
