package gsis;

import java.util.ArrayList;
import java.util.Collections;

import base.Driver;
import base.Measure;
import base.Params;
import base.Simulation;

public class DriverPSP extends Driver {
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

		int publishers = 1;
		//int users = 0;
		int users = Simulation.rand.nextInt(80) + 20;
		int objects = Simulation.rand.nextInt(350) + 50;
		int regions = Simulation.rand.nextInt(4) + 2;
		InitPlaystationPlusClean starter = new InitPlaystationPlusClean(publishers, users, objects, regions);
		param.starter = starter;

		String[] impls = {
			"gsis.ImplementPspInRbac1", "gsis.indirect.ImplementPspInRbac0",
			"gsis.indirect.ImplementPspInUgo"
		};
		param.implementationClassNames = impls;

		// Counts
		param.measures.add("measures.NumberOfActors");
		param.measures.add("measures.NumberOfRegions");
		param.measures.add("measures.NumberOfObjects");
		// Scheme IO

		param.measures.add("measures.SchemeIOAddGAvg");
		param.measures.add("measures.SchemeIOAddOAvg");
		param.measures.add("measures.SchemeIOLJoinAvg");
		param.measures.add("measures.SchemeIOSLeaveAvg");
		param.measures.add("measures.SchemeIOLAddAvg");
		param.measures.add("measures.SchemeIOSRemoveAvg");
		param.measures.add("measures.SchemeIOLRemoveAvg");
		param.measures.add("measures.SchemeIO");

		// Workload IO
		param.measures.add("measures.WorkloadIOAddGAvg");
		param.measures.add("measures.WorkloadIOAddOAvg");
		param.measures.add("measures.WorkloadIOLJoinAvg");
		param.measures.add("measures.WorkloadIOSLeaveAvg");
		param.measures.add("measures.WorkloadIOLAddAvg");
		param.measures.add("measures.WorkloadIOSRemoveAvg");
		param.measures.add("measures.WorkloadIOLRemoveAvg");
		param.measures.add("measures.WorkloadIO");

		param.measures.add("measures.InstancesSLeave");

		param.measures.add("measures.MaxStateSizeMeasure");
		param.measures.add("measures.MaxWStateSizeMeasure");
		param.measures.add("measures.RBAC_MaxRoles");
		param.measures.add("measures.SumStutterMeasure");
		param.measures.add("measures.WorkloadStateChangeProportion");
		param.measures.add("measures.ACStateChangeProportion");
		param.measures.add("measures.PermissionsPerRoleMeasure");

		param.workflowName = "gsis.WorkflowTrivial";
		param.timelimitPerSimulation = 8760 ; //30 days
		param.delta = 12; //12 hours

		return param;
	}

}
