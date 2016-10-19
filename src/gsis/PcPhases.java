package gsis;

import base.Conversion;
import base.SimLogger.Log;

public class PcPhases {
	public enum Phase {
		Create,
		Recruit,
		Submit,
		Review,
		Discuss,
		Notify
	}

	static int dur(Phase p) {
		switch(p) {
			case Create:
				return 1 * Conversion.Weeks;
			case Recruit:
				return 2 * Conversion.Weeks;
			case Submit:
				return 2 * Conversion.Months;
			case Review:
				return 1 * Conversion.Months;
			case Discuss:
				return 2 * Conversion.Weeks;
			case Notify:
				return 1 * Conversion.Months;
		}
		return -1;
	}

	static int totalDur() {
		int ret = 0;
		for(Phase p: Phase.values()) {
			ret += dur(p);
		}
		return ret;
	}

	static double phaseTimeElapsed(double currentTime) {
		boolean seen = false;
		double tot = 0;
		Phase currentPhase = phase(currentTime);
		for(Phase p: Phase.values()) {
			if(p == currentPhase) {
				seen = true;
			}
			if(!seen) {
				tot += dur(p);
			}
		}
		return currentTime - tot;
	}

	static double phaseTimeLeft(double currentTime) {
		boolean seen = false;
		double tot = 0;
		Phase currentPhase = phase(currentTime);
		for(Phase p: Phase.values()) {
			if(!seen) {
				tot += dur(p);
			}
			if(p == currentPhase) {
				seen = true;
			}
		}
		return tot - currentTime;
	}

	public static void main(String[] argv) {
		for(double i = 0; i < totalDur(); i++) {
			if(dur(phase(i)) != phaseTimeElapsed(i) + phaseTimeLeft(i)) {
				Log.e("Error:" +
					phase(i) + " " +
					dur(phase(i)) + " " +
					phaseTimeElapsed(i) + " "+
					phaseTimeLeft(i));
			}
		}
	}

	static Phase phase(double currentTime) {
		if(currentTime < dur(Phase.Create))
			return Phase.Create;
		currentTime -= dur(Phase.Create);

		if(currentTime < dur(Phase.Recruit))
			return Phase.Recruit;
		currentTime -= dur(Phase.Recruit);

		if(currentTime < dur(Phase.Submit))
			return Phase.Submit;
		currentTime -= dur(Phase.Submit);

		if(currentTime < dur(Phase.Review))
			return Phase.Review;
		currentTime -= dur(Phase.Review);

		if(currentTime < dur(Phase.Discuss))
			return Phase.Discuss;
		currentTime -= dur(Phase.Discuss);

		if(currentTime < dur(Phase.Notify))
			return Phase.Notify;
		currentTime -= dur(Phase.Notify);

		return Phase.Notify;
	}
}

