package gsis;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import base.Action;
import base.Measure;
import base.Params;

public class DeterministicUGOTester {

	base.System rbacugo, rbacrhrbac;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new DeterministicUGOTester();

	}

	public DeterministicUGOTester() {
		Params param = new Params();

		InitRbacClean starter = new InitRbacClean(1, 0, 0);
		String[] impls = {"gsis.RbacUGO", "gsis.RbacRHRbac"};
		param.starter = starter;
		param.implementationClassNames = impls;
		param.timelimitPerSimulation=8 * 60;
		param.delta = 1;
		param.workflowName = "gsis.WorkflowTrivial";
		param.create_post_factor = 2;
		param.churn_factor = 1;
		param.del_factor = 1;
		param.post_freqm = 1;
		rbacugo = new base.System("gsis.RbacUGO", param);
		InitRbacRHClean starter2 = new InitRbacRHClean(1, 0, 0);
		param.starter = starter2;
		rbacrhrbac = new base.System("gsis.RbacRHRbac", param);

		java.lang.System.out.println("\nt1 addR(r1)");
		String[] param1 = {"m0", "r1"};
		Action a = new Action("addR", param1);
		execute(a);
		printResults(a);

		java.lang.System.out.println("\nt2 addP(r1)");
		String[] param2 = {"m0", "p1"};
		a = new Action("addP", param2);
		execute(a);
		printResults(a);

		java.lang.System.out.println("\nt2 assignUser(m0, r1)");
		String[] param3 = {"m0","m0", "r1"};
		a = new Action("assignUser", param3);
		execute(a);
		printResults(a);

		java.lang.System.out.println("\nt2 assignPermission(r1, p1)");
		String[] param4 = {"m0", "r1", "p1"};
		a = new Action("assignPermission", param4);
		execute(a);
		printResults(a);

		java.lang.System.out.println("\nt2 addR(r2)");
		String[] param5 = {"m0", "r2"};
		a = new Action("addR", param5);
		execute(a);
		printResults(a);

		java.lang.System.out.println("\nt2 addP(p2)");
		String[] param6 = {"m0", "p2"};
		a = new Action("addP", param6);
		execute(a);
		printResults(a);

		java.lang.System.out.println("\nt2 addHierarchy(r1, r2)");
		String[] param7 = {"m0", "r1", "r2"};
		a = new Action("addHierarchy", param7);
		execute(a);
		printResults(a);

		java.lang.System.out.println("\nt2 assignPermission(r2, p2)");
		String[] param9 = {"m0", "r2", "p2"};
		a = new Action("assignPermission", param9);
		execute(a);
		printResults(a);

		java.lang.System.out.println("\nt2 removeHierarchy(r1, r2)");
		String[] param8 = {"m0", "r1", "r2"};
		a = new Action("removeHierarchy", param8);
		execute(a);
		printResults(a);

	}

	public void execute(Action a) {
		java.lang.System.out.println();
		java.lang.System.out.println("Executing action " + a);
		rbacugo.deterministicExecute(a);
		rbacrhrbac.deterministicExecute(a);
	}

	public void printResults(Action a) {
		java.lang.System.out.print("RbacUGO --  ");
		List<Measure> measures = new ArrayList<Measure>();
		measures.addAll(rbacugo.measures);
		Collections.sort(measures);
		for(Measure m : measures) {
			java.lang.System.out.print(m.getMeasureName() + ": " + m.getCurMeasurement() + "   ");
		}
		java.lang.System.out.println();

		java.lang.System.out.print("RbacRHRbac --  ");
		measures = new ArrayList<Measure>();
		measures.addAll(rbacrhrbac.measures);
		Collections.sort(measures);
		for(Measure m : measures) {
			java.lang.System.out.print(m.getMeasureName() + ": " + m.getCurMeasurement() + "   ");
		}
		java.lang.System.out.println();
	}
}
