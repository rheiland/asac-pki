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
 * PC Members:
 * 1) PC members join committees
 * 2) Nothing to do
 * 3) PC members may read papers and be assigned papers to review.
 * PC members may not read or assigned review of their own papers.
 * (How to check this?)
 * 4) PC members may add/read discussions and read/write reviews, and modify their own reviews.
 * PC members may not do this for their own papers.
 * 5) PC members have access to all reviews/discussions
 * @author Yechen
 *
 */
public class ActorPCMember extends ActorMachine{

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
			//No specific time for PC members, controlled by freq only.
			return 0;
		}
		throw new InvalidMeasureException(p);
	}

	@Override
	public Action nextAction(Params params) {
		double coin = Simulation.rand.nextDouble() / params.delta;
		double thresholdJoin = 0.5 / 12; //1/2 per day by default
		double thresholdReadPaper = 1.0 / 12;
		double thresholdAssignPaper = 0.5 / 12;
		double thresholdWriteDiscussion = 2 / 12;
		double thresholdWriteReview = 0.1 / 12;
		double thresholdReadDiscussion = 1 / 12;
		double thresholdReadReview = 1 / 12;
		double thresholdModifyReview = 0.2 / 12;

		switch(ActorPCDaemon.CURRENT_PHASE){
		case 1:
			//Recruit
			if (coin < thresholdJoin){
				return new Action("JoinCommittee", actor);
			}
			return null;
		case 2:
			//Submission
			return null;
		case 3:
			//Assign Review
			if (coin < thresholdReadPaper){
				//can only read own paper
				return new Action("ReadPaper", actor);
			}
			coin -= thresholdReadPaper;
			if (coin < thresholdAssignPaper){
				return new Action("AssignPaper", actor);
			}
			return null;
		case 4:
			//Review
			if (coin < thresholdReadReview){
				return new Action("ReadReview", actor);
			}
			coin -= thresholdReadReview;
			if (coin < thresholdReadDiscussion){
				return new Action("ReadDiscussion", actor);
			}
			coin -= thresholdReadDiscussion;
			if (coin < thresholdWriteReview){
				return new Action("WriteReview", actor);
			}
			coin -= thresholdWriteReview;
			if (coin < thresholdModifyReview){
				return new Action("ModifyReview", actor);
			}
			coin -= thresholdModifyReview;
			if (coin < thresholdWriteDiscussion){
				return new Action("WriteDiscussion", actor);
			}
			return null;
		case 5:
			//Notify
			if (coin < thresholdReadReview){
				return new Action("ReadReview", actor);
			}
			coin -= thresholdReadReview;
			if (coin < thresholdReadDiscussion){
				return new Action("ReadDiscussion", actor);
			}
			return null;
		}
		return null;
	}

	@Override
	public String getPrefix() {
		return "C";
	}

}
