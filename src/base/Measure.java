package base;

import java.lang.reflect.Method;

/**
 * This is the measure class. All subclasses represent a particular measure we are concerned about.
 * The comparable is interface merely sorts measures by name for pretty printing.
 *
 */
public abstract class Measure implements Comparable<Measure> {
	protected Cost curMeasurement;

	/**
	 * @return the current cost object. Casting is usually required.
	 */
	public Object getCurCost() {
		return curMeasurement.getCost();
	}

	/**
	 * @return the string representation of the current cost. Used in printing.
	 */
	public String getCurMeasurement() {
		return curMeasurement.getCostStr();
	}

	@Override
	public int compareTo(Measure o) {
		return this.getMeasureName().compareTo(o.getMeasureName());
	}

	/**
	 *
	 * @return the name of the measurement in simulator.
	 */
	public String getMeasureName() {
		return this.getClass().getName();
	}

	/**
	 *
	 * @return the "real" name of the measurement.
	 */
	public abstract String getPrintFriendlyName();

	private Method getMethod(String m, Class<?>... parameterTypes) {
		try {
			Method method = this.getClass().getMethod(m, parameterTypes);
			return method;
		} catch(NoSuchMethodException e) {
		}
		return null;
	}

	/**
	 * Get measurement from measurable w. Exception may be thrown if w cannot measure it.
	 * These measurement commands are being executed BEFORE the action is executed in System.
	 * All subclasses must override this, but it may be empty.
	 * @param w the measurable class to measure from.
	 */
	public void preExecMeasurement(Measurable w) {}

	Method preExecMethod = getMethod("preExecMeasurement", Measurable.class);

	public boolean overridesPreExecMeasurement() {
		return preExecMethod.getDeclaringClass() != Measure.class;
	}

	/**
	 * Get measurement from measurable w. Exception may be thrown if w cannot measure it.
	 * These measurement commands are being executed AFTER the action is executed in the system.
	 * All subclasses must override this, but it may be empty.
	 * @param w the measurable class to measure from.
	 */
	public void postExecMeasurement(Measurable w) {}

	Method postExecMethod = getMethod("postExecMeasurement", Measurable.class);

	public boolean overridesPostExecMeasurement() {
		return postExecMethod.getDeclaringClass() != Measure.class;
	}

	/**
	 * Checks whether we can actually measure from class w.
	 * @param w
	 * @return
	 */
	public abstract boolean isMeasurementvalid(Measurable w);
}
