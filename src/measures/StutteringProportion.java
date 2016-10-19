package measures;

import costs.DoubleReplaceCost;
import base.Measurable;
import base.Measure;

public class StutteringProportion extends Measure {
	Measure stut = new StutteringActions();
	Measure wkl = new WorkloadActions();

	public StutteringProportion() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Stutter_Prop";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Proportion of actions that stutter";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		stut.preExecMeasurement(w);
		wkl.preExecMeasurement(w);
	}

	@Override
	public void postExecMeasurement(Measurable w) {
		stut.postExecMeasurement(w);
		wkl.postExecMeasurement(w);

		int stutCur = (Integer)stut.getCurCost();
		double workloadCur = (Integer)wkl.getCurCost();

		curMeasurement.aggregate(stutCur / workloadCur);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return stut.isMeasurementvalid(w) && wkl.isMeasurementvalid(w);
	}

}
