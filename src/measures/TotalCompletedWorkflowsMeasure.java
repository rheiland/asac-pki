package measures;

import java.util.LinkedHashSet;

import costs.IntReplaceCost;

import base.Measurable;
import base.Measure;
import base.Property;
import base.Workflow;

public class TotalCompletedWorkflowsMeasure extends Measure {

	public TotalCompletedWorkflowsMeasure() {
		curMeasurement = new IntReplaceCost();
	}

	@Override
	public String getMeasureName() {
		return "Number_Of_Completed_Workflows";
	}

	@Override
	public void preExecMeasurement(Measurable w) {
		Property p = new Property(Workflow.class, "GetCompletedItems", new String[0]);
		LinkedHashSet<Workflow> workflows = (LinkedHashSet<Workflow>) w.getCurMeasure(p);
		curMeasurement.aggregate(workflows.size());
	}

	@Override
	public void postExecMeasurement(Measurable w) {


	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		Property p = new Property(Workflow.class, "GetCompletedItems", new String[0]);
		return w.isMeasurable(p);
	}

	@Override
	public String getPrintFriendlyName() {
		return "Number of Completed Workflows";
	}

}
