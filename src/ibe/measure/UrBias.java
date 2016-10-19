package ibe.measure;

import base.Measurable;
import base.Measure;
import base.Property;
import costs.DoubleReplaceCost;

public class UrBias extends Measure implements AskAdmin {

	public UrBias() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getPrintFriendlyName() {
		return "Bias toward UR management";
	}

	@Override
	public void postExecMeasurement(Measurable w) {
		Property p = UR_BIAS_PROPERTY;
        Double aR = (Double) w.getCurMeasure(p);
		curMeasurement.aggregate(aR);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		Property p = UR_BIAS_PROPERTY;
		return w.isMeasurable(p);
	}

}
