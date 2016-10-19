package measures;

import costs.IntSumCost;
import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.WorkloadScheme;

public class WorkloadIOLJoin extends Measure {
	Measure io = new WorkloadCurIOMeasure();

	public WorkloadIOLJoin() {
		curMeasurement = new IntSumCost();
	}

	@Override
	public String getMeasureName() {
		return "Workload_LJoin_IO";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Total workload I/O for LJoin operations";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		io.preExecMeasurement(w);
	}

	@Override
	public void postExecMeasurement(Measurable w) {
		Property p = new Property(WorkloadScheme.class, WorkloadScheme.Q_LASTACTION, new String[0]);
		Action a = (Action) w.getCurMeasure(p);
		if(a != null) {
			if(!a.isQuery && a.name.equals("LJoin")) {
				io.postExecMeasurement(w);
				curMeasurement.aggregate((Integer) io.getCurCost());
			}
		}
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		Property p = new Property(WorkloadScheme.class, WorkloadScheme.Q_LASTACTION, new String[0]);
		return w.isMeasurable(p) && io.isMeasurementvalid(w);
	}

}
