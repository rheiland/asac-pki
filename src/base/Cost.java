package base;

/**
 * This represents a particular cost monoid.
 *
 * @param <T> The type of the set of all elements in the monoid.
 * @author yechen
 */
public abstract class Cost<T> {
	/**
	 * This is my current cost.
	 */
	protected T cost;

	/**
	 * Construct Cost class and set cost to identity of the monoid.
	 * subclasses must rewrite this.
	 *
	 * Usually it's just
	 * cost = (identity element in the monoid).
	 *
	 * For example, if T = Integer, and operator is "Max", then we should have
	 * cost = new Integer(Integer.MIN)
	 * On the other hand, if T = Unsigned Integer, and operand is "Max" or "+", we should have
	 * cost = 0
	 * If T=Integer and operand is "*", then we should have cost = 1
	 *
	 * Expect plotting difficulties if id is far away from expected values (Max number of roles: Dac/Rbac/Rbac2)
	 * Automated plotting cannot handle this exception.
	 * In the example above, we should use UnsignedIntMax instead, in which DAC will report # of roles = 0.
	 */
	public Cost() {

	}

	/**
	 * Copy constructor, no override necessary.
	 * @param val
	 */
	public Cost(T val) {
		cost = val;
	}

	/**
	 * Copy consturctor, no override necessary.
	 * @param copy
	 */
	public Cost(Cost<T> copy) {
		cost = copy.getCost();
	}

	/**
	 * Aggregate the other cost to this cost. No override necessary.
	 * @param other the other cost. An exception will be thrown if the cost cannot be aggregated.
	 */
	public void aggregate(Cost<T> other) {
		aggregate(other.getCost());
	}

	/**
	 * This is the actual aggregation of one cost onto another.
	 * Subclasses must implement the actual aggregation.
	 * For example, max would set cost = Math.max(cost, other)
	 * @param other the other operand.
	 */
	public abstract void aggregate(T other);

	/**
	 * No override needed.
	 * @return the cost object
	 */
	public T getCost() {
		return cost;
	}

	/**
	 * Subclass must implement this.
	 * Used when simulation actually prints this to file/console.
	 * @return the string representation of the cost object.
	 */
	public abstract String getCostStr();

}
