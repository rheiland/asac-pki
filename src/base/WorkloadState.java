package base;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the state of the simulated scheme.
 * @author yechen
 *
 */
public abstract class WorkloadState implements Measurable, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -3470469324694829482L;

	private Map<String, Object> curMeasures = new HashMap<String, Object>();

	protected int accessR = 0, accessW = 0;
	public static final String Q_ICOST = "LastInputCost", Q_OCOST = "LastOutputCost";

	/**
	 * Sets I/O so that
	 * read = read + deltaR
	 * write = write + deltaW
	 * @param deltaR
	 * @param deltaW
	 */
	public void incrementAccessIO(int deltaR, int deltaW) {
		if (Implementation.accountIO){
			accessR += deltaR;
			accessW += deltaW;
		}
	}

	public abstract WorkloadState createCopy();

	@Override
	public Set<Property> getMeasurables() {
		Set<Property> ret = new HashSet<Property>();
		ret.add(new Property(this, Q_ICOST));
		ret.add(new Property(this, Q_OCOST));
		return ret;
	}

	/**
	 * Sets the I/O to 0 because measurement has been taken already before the next
	 * workload command.
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
}
