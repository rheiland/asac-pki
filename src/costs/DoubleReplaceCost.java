package costs;

import base.Cost;

/**
 * Set: [Double.MIN, Double.MAX] (finite since we have finite precision)
 * Identity: any element.
 * Operator: "replaced by" (x replaced by y always sets x to be y.)
 * @author yechen
 *
 */
public class DoubleReplaceCost extends Cost<Double> {
	public DoubleReplaceCost() {
		cost = 0.0;
	}

	public DoubleReplaceCost(Cost<Double> copy) {
		cost = copy.getCost();
	}


	@Override
	public void aggregate(Double other) {
		cost = other;
	}

	@Override
	public String getCostStr() {
		return "" + cost;
	}
}
