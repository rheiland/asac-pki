package base;
import java.util.Set;

/**
 * All classes that have measurable properties within the simulation must implement this interface.
 *
 */
public interface Measurable {
	/**
	 * Get a list of measurement names that are valid for this class.
	 * @return
	 */
	public Set<Property> getMeasurables();

	/**
	 *
	 * @param measurementName
	 * @return true if measurementName can be resolved by this class.
	 */
	public boolean isMeasurable(Property p);

	/**
	 *
	 * @param measurementName
	 * @return the object representing the measurement.
	 */
	public Object getCurMeasure(Property p);
}
