package measures;

import costs.IntSumCost;
import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.WorkloadScheme;

public class WorkloadIOLRemove extends Measure {
	Measure io = new WorkloadCurIOMeasure();

	public WorkloadIOLRemove() {
		curMeasurement = new IntSumCost();
	}

	@Override
	public String getMeasureName() {
		return "Workload_IO_LRemove";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Total workload I/O for LRemove operations";
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
			if(!a.isQuery && a.name.equals("LRemove")) {
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
