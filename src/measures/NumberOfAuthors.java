package measures;
import java.util.Set;

import costs.IntReplaceCost;

import base.ActorMachine;
import base.Measurable;
import base.Measure;
import base.Property;
/**
 * Based on name starting with a
 * @author yechen
 *
 */
public class NumberOfAuthors extends Measure {

	public NumberOfAuthors() {
		curMeasurement = new IntReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Number_Of_Authors";
	}

	@Override
	public void preExecMeasurement(Measurable w) {

	}

	@Override
	public void postExecMeasurement(Measurable w) {
		Property p = new Property(ActorMachine.class, "GetNames", new String[0]);
		Set<String> names = (Set<String>) w.getCurMeasure(p);
		int numAuthors = 0;
		for(String n : names) {
			if(n.startsWith("a")) {
				numAuthors++;
			}
		}
		curMeasurement.aggregate(numAuthors);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		Property p = new Property(ActorMachine.class, "GetNames", new String[0]);
		return w.isMeasurable(p);
	}

	@Override
	public String getPrintFriendlyName() {
		return "Number of Authors";
	}

}
