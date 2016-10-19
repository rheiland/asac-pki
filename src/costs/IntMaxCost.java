package costs;

import base.Cost;

/**
 * Set: [Integer.MIN, Integer.MAX] intersect set of all integers
 * Operator: Max
 * Identity: Integer.MIN
 * @author yechen
 *
 */
public class IntMaxCost extends Cost<Integer> {
	/**
	 * Construct Cost class and set cost to identity of the monoid.
	 */
	public IntMaxCost() {
		cost = Integer.MIN_VALUE;
	}

	/**
	 * Copy consturctor
	 * @param copy
	 */
	public IntMaxCost(Cost<Integer> copy) {
		cost = copy.getCost();
	}

	@Override
	public void aggregate(Integer other) {
		cost = Math.max(cost, other);

	}

	@Override
	public String getCostStr() {
		return "" + cost;
	}
}
