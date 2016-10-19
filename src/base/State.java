package base;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a state of simulated scheme.
 * @author yechen
 *
 */
public abstract class State implements Measurable {
	private Map<String, Object> curMeasures = new HashMap<String, Object>();
	/**
	 * My I/O costs
	 */
	protected int accessR = 0, accessW = 0;
	public static final String Q_ICOST = "LastInputCost", Q_OCOST = "LastOutputCost";


	/**
	 * This returns a copy of the same state.
	 * Every time a command is executed, the old state is stored into prevState.
	 * The new state is a copy of the old state.
	 * This must be implemented carefully. Though the implementation should be straightforward.
	 * @return a copy of the same state as original.
	 */
	public abstract State createCopy();

	@Override
	public Set<Property> getMeasurables() {
		Set<Property> ret = new HashSet<Property>();
		ret.add(new Property(this, Q_ICOST));
		ret.add(new Property(this, Q_OCOST));
		return ret;
	}

	/**
	 * Resets I/O costs to 0.
	 */
	public void newWorkloadCommand() {
		accessR = 0;
		accessW = 0;
	}

	@Override
	public boolean isMeasurable(Property p) {
		Property qTest = new Property(this, "__test");
		if(!qTest.matchQualifier(p)) {
			return false;
		}
		if(p.name.equals(Q_ICOST)) {
			return true;
		}
		if(p.name.equals(Q_OCOST)) {
			return true;
		}
		return false;
	}

	@Override
	public Object getCurMeasure(Property p) {
		Property qTest = new Property(this, "__test");
		if(!qTest.matchQualifier(p)) {
			return false;
		}
		if(p.name.equals(Q_ICOST)) {
			return accessW;
		}
		if(p.name.equals(Q_OCOST)) {
			return accessR;
		}
		throw new InvalidMeasureException(p);
	}

	/**
	 * Increment Access I/O
	 * Sets new reads = reads + deltaR
	 * writes = writes + deltaW
	 * @param deltaR
	 * @param deltaW
	 */
	public void incrementAccessIO(int deltaR, int deltaW) {
		// Only increment if I/O accounting enabled
		if (Implementation.accountIO){
			accessR += deltaR;
			accessW += deltaW;
		}
	}

}
