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
 * Authors:
 * 1) Recurit:
 * Nothing to do in the system.
 * 2) Submission:
 * Authors may submit papers and read their own papers
 * 3) Assign Reviews:
 * Authors may read their own papers
 * 4) Review:
 * Authors may read their own papers
 * 5) Notify:
 * Authors may read their own papers and reviews
 * @author Yechen
 *
 */
public class ActorAuthor extends ActorMachine {

	@Override
	public Set<Property> getMeasurables() {
		Set<Property> ret = new HashSet<Property>();
		ret.add(new ActorProperty(this, Q_TIME, new String[0]));
		return ret;
	}

	@Override
	public boolean isMeasurable(Property p) {
		Property qTest = new ActorProperty(this, "__test", new String[0]);
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
			if(p.params.get(0).contains("SubmitPaper")) {
				return 1 + Simulation.rand.nextInt(5); //change it?
			}
			return 0;
		}
		throw new InvalidMeasureException(p);
	}

	@Override
	public Action nextAction(Params params) {
		double coin = Simulation.rand.nextDouble() / params.delta;
		double thresholdSubmit = 0.2 / 12;
		double thresholdModify = 0.5 / 12;
		double thresholdReadReview = 3.0 / 12;
		double thresholdReadPaper = 1.0 / 12;
		switch(ActorPCDaemon.CURRENT_PHASE){
		case 1:
			//Recruit
			return null;
		case 2:
			//Submission
			if (coin < thresholdSubmit){
				return new Action("SubmitPaper", actor);
			}
			coin -= thresholdSubmit;
			if (coin < thresholdModify){
				return new Action("ModifyPaper", actor);
			}
			coin -= thresholdModify;
			if (coin < thresholdReadPaper){
				//can only read own paper
				return new Action("ReadPaper", actor);
			}
			return null;
		case 3:
		case 4:
			//Assign Review, Review
			if (coin < thresholdReadPaper){
				//can only read own paper
				return new Action("ReadPaper", actor);
			}
			return null;
		case 5:
			//Notify
			if (coin < thresholdReadPaper){
				//can only read own paper
				return new Action("ReadPaper", actor);
			}
			coin -= thresholdReadPaper;
			if (coin < thresholdReadReview){
				return new Action("ReadReview", actor);
			}
			coin -= thresholdReadReview;
			return null;
		}
		return null;
	}

	@Override
	public String getPrefix() {
		return "A";
	}

}
