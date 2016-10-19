package costs;

import base.Cost;


/**
 * Set: [Integer.MIN, Integer.MAX] (finite since we have finite precision)
 * Identity: any element.
 * Operator: "replaced by" (x replaced by y always sets x to be y.)
 * @author yechen
 *
 */
public class IntReplaceCost extends Cost<Integer> {
	/**
	 * Construct Cost class and set cost to identity of the monoid.
	 */
	public IntReplaceCost() {
		cost = 0; //anything is id.
	}

	/**
	 * Copy consturctor
	 * @param copy
	 */
	public IntReplaceCost(Cost<Integer> copy) {
		cost = copy.getCost();
	}

	@Override
	public void aggregate(Integer other) {
		cost = other;

	}

	@Override
	public String getCostStr() {
		return "" + cost;
	}
}
