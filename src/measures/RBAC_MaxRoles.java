package measures;

import costs.IntMaxCost;
import costs.UnsignedIntMaxCost;
import ac.Rbac1State;
import ac.Rbac0State;
import ac.UgoState;
import base.Cost;
import base.InvalidMeasureException;
import base.Measurable;
import base.Measure;
import base.Property;

public class RBAC_MaxRoles extends Measure {

	public RBAC_MaxRoles() {
		curMeasurement = new UnsignedIntMaxCost();
	}

	@Override
	public String getMeasureName() {
		return "Max_Number_of_Roles";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Maximum Number of Roles";
	}

	@Override
	public void preExecMeasurement(Measurable w) {

	}

	@Override
	public void postExecMeasurement(Measurable w) {

		Cost<Integer> measure = (Cost<Integer>) curMeasurement;;
		Integer size = null;
		if(w.isMeasurable(new Property(Rbac1State.class, "sizeof", new String[0]))) {
			String[] param = {"R"};
			Property p = new Property(Rbac1State.class, "sizeof", param);
			size = (Integer) w.getCurMeasure(p);
		} else if(w.isMeasurable(new Property(Rbac0State.class, "sizeof", new String[0]))) {
			String[] param = {"R"};
			Property p = new Property(Rbac0State.class, "sizeof", param);
			size = (Integer) w.getCurMeasure(p);
		} else if(w.isMeasurable(new Property(UgoState.class, "sizeof", new String[0]))) {
			String[] param = {"G"};
			Property p = new Property(UgoState.class, "sizeof", param);
			size = (Integer) w.getCurMeasure(p);
		}
		measure.aggregate(size);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return w.isMeasurable(new Property(Rbac1State.class, "R", new String[0])) ||
		       w.isMeasurable(new Property(UgoState.class, "G", new String[0])) ||
		       w.isMeasurable(new Property(Rbac0State.class, "R", new String[0]));
	}

}
