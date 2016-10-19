package base;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import base.SimLogger.Log;
/**
 * This class represents a particular simulation.
 * Multiple systems may be present in a given simulation.
 * @author yechen
 *
 */
public class Simulation {
	double time;
	public static final Random rand = new Random();
//	public static final int caDepth = 2;
	public int caDepth = 2;
	final String sep = "\t";
	Params params;

	List<System> systems = new ArrayList<System>();

	public Simulation(Params param) {
		this.params = param;
	}

	/**
	 * Resets the time to run another sim.
	 */
	public void reset() {
		time = 0;
	}

	/**
	 * Rebuild all system to starting parameters.
	 */
	public void build() {
		systems.clear();
		for(String impl : params.implementationClassNames) {
			Params p = new Params(params);
			System sys = new System(impl, p);
			systems.add(sys);
		}
	}

	/**
	 * Print the header
	 * @param br do we print "\n"
	 */
	public void header(Boolean br) {
		for(System s : systems) {
			for(Measure m : s.measures) {
				java.lang.System.out.print(sep + s.getImplementationName() + "-" + m.getMeasureName());
			}
		}
		if(br) {
			java.lang.System.out.println();
		}
	}

	/**
	 * Return the header string
	 * @param br do we include "\n"
	 * @return the string rep. of header
	 */
	public String headerStr(Boolean br) {
		String ret = "";
		for(System s : systems) {
			for(Measure m : s.measures) {
				ret += sep + s.getImplementationName() + "-" + m.getMeasureName();
			}
		}
		if(br) {
			ret += "\n";
		}
		return ret;
	}

	/**
	 * @param br do we include "\n"
	 * @return return the string rep. of results
	 */
	public String results(Boolean br) {
		String ret = "";
		for(System s : systems) {
			for(Measure m : s.measures) {
				ret += (sep + m.getCurMeasurement());
			}
		}

		if(br) {
			ret += "\n";
		}
		return ret;
	}
	/**
	 * Run all systems until expiration.
	 * After this finishes, you can get results with results() method.
	 * This is used to run one simulation.
	 * To run another after this, reset is required.
	 */
	public void run() {
        java.lang.System.out.println("(rwh)Simulation.run(): changing params.timelimitPerSim to be 42");
		params.timelimitPerSimulation = 37;   // 42;
		while(time < params.timelimitPerSimulation) {
			boolean someoneStopped = false;
			for(System system : systems) {
				// Check if any of the systems has stopped the clock
				if(system.TIME_STOPPED) someoneStopped = true;
			}
			if(!someoneStopped) {
				// If no clocks are stopped, increment global clock and advance all systems
				time += params.delta;
				for(System system : systems) {
					system.advance(time);
				}
			} else {
				// If any system has stopped, do not increment clock; advance only stopped systems
				for(System system : systems) {
					if(system.TIME_STOPPED) {
						system.advance(time);
					}
				}
			}
			Log.i("CURRENT TIME: " + time + " (" + ((params.timelimitPerSimulation - time) / params.delta) + " steps left)");
		}
	}
}