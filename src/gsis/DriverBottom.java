package gsis;

import java.util.ArrayList;
import java.util.Collections;

import base.Driver;
import base.Measure;
import base.Params;
import base.Simulation;

public class DriverBottom extends Driver {
	static final double CHURN_MAX = 4.0;
	static final double POSTF_MAX = 3.0;
	static final double DEL_MAX = 1.0;
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
		int managers, posters, readers;
		posters = Simulation.rand.nextInt(40) + 25;
		readers = 0;
		managers = (int)(posters * Simulation.rand.nextDouble()/5.0) + 5;

		InitBottomClean starter = new InitBottomClean(managers, posters, readers);
		param.starter = starter;
		String[] impls = {
			"gsis.ImplementBottomInRbac1", "gsis.indirect.ImplementBottomInRbac0", "gsis.indirect.ImplementBottomInUgo"
		};
		param.implementationClassNames = impls;

		// Counts
		param.measures.add("measures.NumberOfActors");
		param.measures.add("measures.NumberOfPosters");
		param.measures.add("measures.NumberOfManagers");
		param.measures.add("measures.NumberOfObjects");

		// Scheme IO
		param.measures.add("measures.SchemeIOAddGAvg");
		param.measures.add("measures.SchemeIOAddOAvg");
		param.measures.add("measures.SchemeIOLJoinAvg");
		param.measures.add("measures.SchemeIOLLeaveAvg");
		param.measures.add("measures.SchemeIOLAddAvg");
		param.measures.add("measures.SchemeIOLRemoveAvg");
		param.measures.add("measures.SchemeIO");

		// Workload IO
		param.measures.add("measures.WorkloadIOAddGAvg");
		param.measures.add("measures.WorkloadIOAddOAvg");
		param.measures.add("measures.WorkloadIOLJoinAvg");
		param.measures.add("measures.WorkloadIOLLeaveAvg");
		param.measures.add("measures.WorkloadIOLAddAvg");
		param.measures.add("measures.WorkloadIOLRemoveAvg");
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
		param.create_post_factor = 1;
		param.churn_factor = 1;
		param.del_factor = 1;
		param.post_freqm = 1;
		param.timelimitPerSimulation=24 * 3;
		param.delta = 1;
		param.workflowName = "gsis.WorkflowTrivial";

		return param;
	}

}
