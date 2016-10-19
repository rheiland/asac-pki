package measures;

import costs.IntSumCost;
import base.State;
import base.Measurable;
import base.Measure;
import base.Property;

public class SchemeIO extends Measure {

	public SchemeIO() {
		curMeasurement = new IntSumCost();
	}
	@Override
	public String getMeasureName() {
		return "Scheme_IO";
	}
	
	@Override
	public String getPrintFriendlyName() {
		return "Total scheme I/O";
	}

	@Override
	public void preExecMeasurement(Measurable w) {


	}

	@Override
	public void postExecMeasurement(Measurable w) {
		Property p = new Property(State.class, State.Q_ICOST, new String[0]);
		curMeasurement.aggregate(w.getCurMeasure(p));
		p = new Property(State.class, State.Q_OCOST, new String[0]);
		curMeasurement.aggregate(w.getCurMeasure(p));
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		Property p1 = new Property(State.class, State.Q_ICOST, new String[0]);
		Property p2 = new Property(State.class, State.Q_OCOST, new String[0]);
		return w.isMeasurable(p1) && w.isMeasurable(p2);
	}

}
