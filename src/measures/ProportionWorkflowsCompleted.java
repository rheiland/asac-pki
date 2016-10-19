package measures;

import java.util.LinkedHashSet;

import costs.DoubleReplaceCost;

import base.Measurable;
import base.Measure;
import base.Property;
import base.Workflow;

public class ProportionWorkflowsCompleted extends Measure {

	public ProportionWorkflowsCompleted() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "CompletedWorkflowRate";
	}

	@Override
	public String getPrintFriendlyName() {
		return "Proportion of Workflows Completed";
	}

	@Override
	public void preExecMeasurement(Measurable w) {

	}

	@Override
	public void postExecMeasurement(Measurable w) {
		Property p = new Property(Workflow.class, "GetCompletedItems", new String[0]);
		LinkedHashSet<Workflow> completed = (LinkedHashSet<Workflow>) w.getCurMeasure(p);
		Property p2 = new Property(Workflow.class, "GetItems", new String[0]);
		LinkedHashSet<Workflow> incomplete = (LinkedHashSet<Workflow>) w.getCurMeasure(p2);
		double completedSize = (double) completed.size();
		double incompleteSize = (double) incomplete.size();
		curMeasurement.aggregate((completedSize) / (completedSize + incompleteSize));
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		Property p = new Property(Workflow.class, "GetCompletedItems", new String[0]);
		Property p2 = new Property(Workflow.class, "GetItems", new String[0]);
		return w.isMeasurable(p) && w.isMeasurable(p2);
	}

}
