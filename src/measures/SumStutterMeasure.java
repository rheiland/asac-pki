package measures;

import java.util.ArrayList;

import costs.IntSumCost;
import base.Scheme;
import base.Action;
import base.Measurable;
import base.Measure;
import base.Property;

public class SumStutterMeasure extends Measure {

	public SumStutterMeasure() {
		curMeasurement = new IntSumCost();
	}

	@Override
	public String getMeasureName() {
		return "Total_Stutter_Cost";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		// TODO Auto-generated method stub

	}
	@Override
	public void postExecMeasurement(Measurable w) {
		Property p = new Property(Scheme.class, Scheme.Q_ACTIONSEXECUTED, new String[0]);
		ArrayList<Action> arr = (ArrayList<Action>) w.getCurMeasure(p);
		int stutter = Math.max(0, arr.size() - 1);
		curMeasurement.aggregate(stutter);
	}
	@Override
	public boolean isMeasurementvalid(Measurable w) {
		Property p = new Property(Scheme.class, Scheme.Q_ACTIONSEXECUTED, new String[0]);
		return w.isMeasurable(p);
	}

	@Override
	public String getPrintFriendlyName() {
		return "Total Stutter Cost";
	}

}
