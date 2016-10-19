package costs;

import base.Cost;

/**
 * Set: [Integer.MIN, Integer.MAX] (finite since we have finite precision)
 * Identity: 0
 * Operator: +
 * @author yechen
 *
 */
public class IntSumCost extends Cost<Integer> {

	/**
	 * Construct Cost class and set cost to identity of the monoid.
	 */
	public IntSumCost() {
		cost = 0;
//		cost = 42;  //rwh
	}

	/**
	 * Copy consturctor
	 * @param copy
	 */
	public IntSumCost(Cost<Integer> copy) {
		cost = copy.getCost();
	}

	@Override
	public void aggregate(Integer other) {
		cost += other;

	}

	@Override
	public String getCostStr() {
		return "" + cost;
	}

}
