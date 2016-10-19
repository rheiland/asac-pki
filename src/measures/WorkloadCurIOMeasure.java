package measures;

import costs.IntReplaceCost;
import base.Measurable;
import base.Measure;
import base.Property;
import base.WorkloadState;

public class WorkloadCurIOMeasure extends Measure {
	public WorkloadCurIOMeasure() {
		curMeasurement = new IntReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Last_Workload_IO";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Last Workload IO";
	}

	@Override
	public void preExecMeasurement(Measurable w) {

	}

	@Override
	public void postExecMeasurement(Measurable w) {
		Property in = new Property(WorkloadState.class, WorkloadState.Q_ICOST, new String[0]);
		Property out = new Property(WorkloadState.class, WorkloadState.Q_OCOST, new String[0]);
		Integer numIn = (Integer) w.getCurMeasure(in);
		Integer numOut = (Integer) w.getCurMeasure(out);
		Integer total = numIn + numOut;
		curMeasurement.aggregate(total);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return w.isMeasurable(new Property(WorkloadState.class, WorkloadState.Q_ICOST, new String[0]))
		       &&
		       w.isMeasurable(new Property(WorkloadState.class, WorkloadState.Q_OCOST, new String[0]));
	}

}
