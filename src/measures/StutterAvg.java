package measures;

import costs.DoubleReplaceCost;
import base.Measurable;
import base.Measure;

public class StutterAvg extends Measure {
	Measure sch = new SchemeActions();
	Measure wkl = new WorkloadActions();

	public StutterAvg() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Stutter_Avg";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Average stutter cost per operation";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		sch.preExecMeasurement(w);
		wkl.preExecMeasurement(w);
	}

	@Override
	public void postExecMeasurement(Measurable w) {
		sch.postExecMeasurement(w);
		wkl.postExecMeasurement(w);

		int schemeCur = (Integer)sch.getCurCost();
		double workloadCur = (Integer)wkl.getCurCost();

		curMeasurement.aggregate(schemeCur / workloadCur);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return sch.isMeasurementvalid(w) && wkl.isMeasurementvalid(w);
	}

}
