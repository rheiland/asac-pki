package measures;

import costs.IntSumCost;
import costs.UnsignedIntMaxCost;
import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.WorkloadScheme;

public class WorkloadAuthIOMeasure extends Measure {
	Measure io = new WorkloadCurIOMeasure();

	public WorkloadAuthIOMeasure() {
		curMeasurement = new IntSumCost();
	}

	@Override
	public String getMeasureName() {
		return "Workload_Auth_IO";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Workload Auth IO";
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
			if(a.isQuery && a.name.equals("auth")) {
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
