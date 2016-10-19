package measures;

import costs.DoubleReplaceCost;
import base.Measurable;
import base.Measure;

public class ACStateChangeProportion extends Measure {
	Measure writes = new ACSumWriteMeasure();
	Measure sSize = new SumStateSizeMeasure();

	public ACStateChangeProportion() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Proportion_AC_State_Change";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Proportion of AC State Change";
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

		Integer numWrites = (Integer) writes.getCurCost();
		Integer numSize = (Integer) sSize.getCurCost();

		Double ratio = new Double((double) numWrites / numSize);
		curMeasurement.aggregate(ratio);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return writes.isMeasurementvalid(w) && sSize.isMeasurementvalid(w);
	}

}
