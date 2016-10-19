package costs;

import base.Cost;

/**
 * Set: [Double.MIN, Double.MAX] (finite since we have finite precision)
 * Identity: any element.
 * Operator: "replaced by" (x replaced by y always sets x to be y.)
 * @author yechen
 *
 */
public class UnsignedDoubleMaxCost extends Cost<Double> {
	public UnsignedDoubleMaxCost() {
		cost = 0.0;
	}

	public UnsignedDoubleMaxCost(Cost<Double> copy) {
		cost = copy.getCost();
	}


	@Override
	public void aggregate(Double other) {
		cost = Math.max(cost, other);
	}

	@Override
	public String getCostStr() {
		return "" + cost;
	}
}
