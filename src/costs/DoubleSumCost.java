package costs;

import base.Cost;

/**
 * Set: [Double.MIN, Double.MAX] (finite since we have finite precision)
 * Identity: 0
 * Operator: +
 * @author yechen
 *
 */
public class DoubleSumCost extends Cost<Double> {

	public DoubleSumCost() {
		cost = 0.0;
	}

	public DoubleSumCost(Cost<Double> copy) {
		cost = copy.getCost();
	}


	@Override
	public void aggregate(Double other) {
		cost += other;
	}

	@Override
	public String getCostStr() {
		return "" + cost;
	}

}
