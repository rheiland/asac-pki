package costs;

import base.Cost;

/**
 * Set: [0, Integer.MAX] intersect set of all integers
 * Operator: Max
 * Identity: 0
 * Will throw exception if we operate on an element outside the range.
 * @author yechen
 *
 */
public class UnsignedIntMaxCost extends Cost<Integer> {

	public UnsignedIntMaxCost() {
		cost = 0;
	}


	/**
	 * Copy consturctor
	 * @param copy
	 */
	public UnsignedIntMaxCost(Cost<Integer> copy) {
		cost = copy.getCost();

		if(copy.getCost() < 0) {
			throw new RuntimeException("Taking in integer < 0 for unsigned integer cost.");
		}
	}

	@Override
	public void aggregate(Integer other) {
		cost = Math.max(cost, other);
		if(other < 0) {
			throw new RuntimeException("Taking in integer < 0 for unsigned integer cost.");
		}
	}

	@Override
	public String getCostStr() {
		return "" + cost;
	}
}
