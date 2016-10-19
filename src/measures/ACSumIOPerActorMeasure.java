package measures;

import java.util.Set;

import costs.DoubleSumCost;
import base.ActorMachine;
import base.Measurable;
import base.Measure;
import base.Property;

public class ACSumIOPerActorMeasure extends Measure {
	SchemeIO total = new SchemeIO();

	public ACSumIOPerActorMeasure() {
		curMeasurement = new DoubleSumCost();
	}
	@Override
	public String getMeasureName() {
		return "IO_Cost_per_Actor";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		total.preExecMeasurement(w);
	}

	@Override
	public void postExecMeasurement(Measurable w) {
		total.postExecMeasurement(w);
		Property p = new Property(ActorMachine.class, "GetNames", new String[0]);
		Set<String> actorNames = (Set<String>) w.getCurMeasure(p);
		int costTotal = (Integer) total.getCurCost();
		curMeasurement.aggregate((double) costTotal / actorNames.size());

	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		Property p = new Property(ActorMachine.class, "GetNames", new String[0]);
		return total.isMeasurementvalid(w) && w.isMeasurable(p);
	}
	@Override
	public String getPrintFriendlyName() {
		return "Total I/O Cost in Simulated Scheme per Actor";
	}

}
