package measures;

import costs.DoubleReplaceCost;
import ac.Rbac1State;
import ac.Rbac0State;
import ac.UgoState;
import base.Cost;
import base.Measurable;
import base.Measure;
import base.Property;

public class PermissionsPerRoleMeasure extends Measure {
	public PermissionsPerRoleMeasure() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Permissions_Per_Role(Replace Percentage)";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Permissions Per Role(Replace Percentage)";
	}

	@Override
	public void preExecMeasurement(Measurable w) {

	}

	@Override
	public void postExecMeasurement(Measurable w) {
		Cost<Double> measure = (Cost<Double>) curMeasurement;;
		Integer sizeR = null, sizeP = null;
		if(w.isMeasurable(new Property(Rbac1State.class, "R", new String[0]))) {
			String[] param = {"R"};
			Property p = new Property(Rbac1State.class, "sizeof", param);
			sizeR = (Integer) w.getCurMeasure(p);
			String[] param2 = {"PA"};
			p = new Property(Rbac1State.class, "sizeof", param2);
			sizeP = (Integer) w.getCurMeasure(p) / 2; //count only permission size, not role size.

		} else if(w.isMeasurable(new Property(Rbac0State.class, "R", new String[0]))) {
			String[] param = {"R"};
			Property p = new Property(Rbac0State.class, "sizeof", param);
			sizeR = (Integer) w.getCurMeasure(p);
			String[] param2 = {"PA"};
			p = new Property(Rbac0State.class, "sizeof", param2);
			sizeP = (Integer) w.getCurMeasure(p) / 2; //count only permission size, not role size.
		} else if(w.isMeasurable(new Property(UgoState.class, "G", new String[0]))) {
			String[] param = {"G"};
			Property p = new Property(UgoState.class, "sizeof", param);
			sizeR = (Integer) w.getCurMeasure(p);
			String[] param2 = {"groupRight"};
			p = new Property(UgoState.class, "sizeof", param2);
			sizeP = (Integer) w.getCurMeasure(p) / 2;
		}

		if(sizeR != 0)
			measure.aggregate((double) sizeP * 100 / sizeR);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return w.isMeasurable(new Property(Rbac1State.class, "R", new String[0])) ||
		       w.isMeasurable(new Property(UgoState.class, "G", new String[0])) ||
		       w.isMeasurable(new Property(Rbac0State.class, "R", new String[0]));
	}

}
