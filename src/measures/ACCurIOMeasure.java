package measures;

import costs.IntReplaceCost;
import base.State;
import base.Measurable;
import base.Measure;
import base.Property;

public class ACCurIOMeasure extends Measure {
	public ACCurIOMeasure() {
		curMeasurement = new IntReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Last_AC_IO_Measure";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Last AC IO Measure";
	}

	@Override
	public void preExecMeasurement(Measurable w) {

	}

	@Override
	public void postExecMeasurement(Measurable w) {
		Property in = new Property(State.class, State.Q_ICOST, new String[0]);
		Property out = new Property(State.class, State.Q_OCOST, new String[0]);
		Integer numIn = (Integer) w.getCurMeasure(in);
		Integer numOut = (Integer) w.getCurMeasure(out);
		Integer total = numIn + numOut;
		curMeasurement.aggregate(total);

	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return w.isMeasurable(new Property(State.class, State.Q_ICOST, new String[0]))
		       &&
		       w.isMeasurable(new Property(State.class, State.Q_OCOST, new String[0]));
	}

}
