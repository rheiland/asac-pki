package gsis;

import java.util.ArrayList;
import java.util.Collections;

import base.Driver;
import base.Measure;
import base.Params;
import base.Simulation;

public class DriverPC extends Driver {
	private Params param;

	@Override
	public int getNumRuns() {
		return super.getNumRuns();
	}

	@Override
	public String generatePlotter() {
		if(param == null)
			return null;
		ArrayList<Measure> plots = new ArrayList<Measure>();
		try {
			for(String mname : param.measures) {
				Class measureClass = Class.forName(mname);
				Measure measure = (Measure) measureClass.newInstance();
				plots.add(measure);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		Collections.sort(plots);

		String ret = super.getPlotter(param.implementationClassNames, plots);

		return ret;
	}

	@Override
	public Params generateSimulation() {
		param = new Params();
		int authors;
		authors = Simulation.rand.nextInt(55) + 20;

		InitProgramCommitteeClean starter = new InitProgramCommitteeClean(authors);
		param.starter = starter;
		String[] impls = {
			"gsis.ImplementPcInRbac1", "gsis.indirect.ImplementPcInRbac0",
			"gsis.indirect.ImplementPcInUgo"
		};
		param.implementationClassNames = impls;

		// Counts
		param.measures.add("measures.NumberOfActors");
		param.measures.add("measures.NumberOfObjects");

		// Scheme IO
		param.measures.add("measures.SchemeIOAddGAvg");
		param.measures.add("measures.SchemeIOAddOAvg");
		param.measures.add("measures.SchemeIOSJoinAvg");
		param.measures.add("measures.SchemeIOLJoinAvg");
		param.measures.add("measures.SchemeIOSLeaveAvg");
		param.measures.add("measures.SchemeIOLLeaveAvg");
		param.measures.add("measures.SchemeIOLAddAvg");
		param.measures.add("measures.SchemeIO");

		// Workload IO
		param.measures.add("measures.WorkloadIOAddGAvg");
		param.measures.add("measures.WorkloadIOAddOAvg");
		param.measures.add("measures.WorkloadIOSJoinAvg");
		param.measures.add("measures.WorkloadIOLJoinAvg");
		param.measures.add("measures.WorkloadIOSLeaveAvg");
		param.measures.add("measures.WorkloadIOLLeaveAvg");
		param.measures.add("measures.WorkloadIOLAddAvg");
		param.measures.add("measures.WorkloadIO");

		param.measures.add("measures.MaxStateSizeMeasure");
		param.measures.add("measures.MaxWStateSizeMeasure");
		param.measures.add("measures.RBAC_MaxRoles");
		param.measures.add("measures.SumStutterMeasure");
		param.measures.add("measures.WorkloadStateChangeProportion");
		param.measures.add("measures.ACStateChangeProportion");
		param.measures.add("measures.PermissionsPerRoleMeasure");

		//If a user created a new group, the frequency of the user creating a single
		//post is multiplied by...
		param.create_post_factor = 2;
		param.churn_factor = 1;
		param.del_factor = 1;
		param.post_freqm = 1;
		param.timelimitPerSimulation = 8 * 30;
		param.delta = 1; //days
		param.workflowName = "gsis.WorkflowTrivial";

		return param;
	}

}
