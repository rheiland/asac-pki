package measures;

import costs.IntSumCost;
import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;
import base.WorkloadScheme;

public class SchemeIOAddG extends Measure {
	Measure io = new ACCurIOMeasure();

	public SchemeIOAddG() {
		curMeasurement = new IntSumCost();
	}

	@Override
	public String getMeasureName() {
		return "Scheme_IO_addG";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Total scheme I/O for addG operations";
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
			if(!a.isQuery && a.name.equals("addG")) {
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
