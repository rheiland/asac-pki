package pc;

import java.util.HashSet;
import java.util.Set;

import base.Action;
import base.ActorMachine;
import base.ActorProperty;
import base.InvalidMeasureException;
import base.Params;
import base.Property;
import base.Simulation;

/**
 * The daemon adds PCs and change phases.
 * @author Yechen
 *
 */
public class ActorPCDaemon extends ActorMachine{
	//Time Unit: 12 hours
	static int CURRENT_PHASE = 1;//Recruit, Submission, Assign Reviews, Review, Notify
	private int tick = 0;

	public Set<Property> getMeasurables() {
		Set<Property> ret = new HashSet<Property>();
		ret.add(new ActorProperty(this, Q_TIME, new String[0]));
		return ret;
	}
	@Override
	public boolean isMeasurable(Property p) {
		Property qTest = new Property(this, "__test", new String[0]);
		if(!qTest.matchQualifier(p)) {
			return false;
		}
		if(p.name.equals(Q_TIME)) return true;
		return false;
	}

	@Override
	public Object getCurMeasure(Property p) {
		Property qTest = new ActorProperty(this, "__test", new String[0]);
		if(!qTest.matchQualifier(p)) {
			throw new InvalidMeasureException(p);
		}
		if(p.name.equals(Q_TIME)) {
			//managements are easy for this actor.
			return 0;
		}
		throw new InvalidMeasureException(p);
	}

	@Override
	public Action nextAction(Params params) {
		tick++;
		Action ret = null;
		//Static Committee?
		double coin = Simulation.rand.nextDouble() / params.delta;
		double thresholdGroupAdd = 1.0 / 12; //1 per day by default
		if (CURRENT_PHASE == 1 && coin < thresholdGroupAdd){
			ret = new Action("AddCommittee");
		}

		if (CURRENT_PHASE == 5)
			return null;
		if (CURRENT_PHASE == 4 && tick >= 120 * 2){
			incrementPhase();
		}
		if (CURRENT_PHASE == 3 && tick >= 15 * 2){
			incrementPhase();
		}
		if (CURRENT_PHASE == 2 && tick >= 110 * 2){
			incrementPhase();
		}
		if (CURRENT_PHASE == 1 && tick >= 110 * 2){
			//At Recurit, Increment to Submissions
			incrementPhase();
		}
		return ret;
	}

	private void incrementPhase(){
		tick = 0;
		CURRENT_PHASE++;
	}

	@Override
	public String getPrefix() {
		return "D";
	}

}
