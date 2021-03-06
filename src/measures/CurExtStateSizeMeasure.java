package measures;

import costs.IntReplaceCost;
import base.State;
import base.Cost;
import base.Implementation;
import base.Measurable;
import base.Measure;
import base.Property;

public class CurExtStateSizeMeasure extends Measure {

	public CurExtStateSizeMeasure() {
		curMeasurement = new IntReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Extension_State_Size";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
	}

	@Override
	public void postExecMeasurement(Measurable w) {
		String[] params = {"ExtALL"};
		Property p = new Property(State.class, "sizeof", params);
		Integer size = (Integer) w.getCurMeasure(p);
		Cost<Integer> measure = (Cost<Integer>) curMeasurement;
		measure.aggregate(size);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		String[] params = {"ExtALL"};
		Property p = new Property(State.class, "sizeof", params);
		return w.isMeasurable(p);
	}

	@Override
	public String getPrintFriendlyName() {
		return "Last Extension Size";
	}

}
