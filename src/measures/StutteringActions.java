package measures;

import java.util.ArrayList;

import costs.IntSumCost;
import base.Scheme;
import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;

public class StutteringActions extends Measure {

	public StutteringActions() {
		curMeasurement = new IntSumCost();
	}

	@Override
	public String getMeasureName() {
		return "Stuttering_Actions";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Total scheme actions which stutter";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		// TODO Auto-generated method stub

	}
	@Override
	public void postExecMeasurement(Measurable w) {
		Property p = new Property(Scheme.class, Scheme.Q_ACTIONSEXECUTED, new String[0]);
		ArrayList<Action> arr = (ArrayList<Action>) w.getCurMeasure(p);
		if(arr.size() > 1) {
			curMeasurement.aggregate(1);
		}
	}
	@Override
	public boolean isMeasurementvalid(Measurable w) {
		Property p = new Property(Scheme.class, Scheme.Q_ACTIONSEXECUTED, new String[0]);
		return w.isMeasurable(p);
	}

}
