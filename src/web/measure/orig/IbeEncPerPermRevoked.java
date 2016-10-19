package ibe.measure;

import costs.DoubleReplaceCost;
import base.Measurable;
import base.Measure;

public class IbeEncPerPermRevoked extends Measure {
	Measure encs = new IbeEncRevP();
	Measure revs = new PermsRevoked();

	public IbeEncPerPermRevoked() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getPrintFriendlyName() {
		return "IBE encryptions per permission revocation";
	}

	@Override
	public void postExecMeasurement(Measurable w) {
        String type = Util.identify(w);

        // Check the implementation
        if(!type.equals("PT")) return;

		encs.postExecMeasurement(w);
		revs.postExecMeasurement(w);

		double encsCost = (Integer)encs.getCurCost();
		double revsCost = (Integer)revs.getCurCost();

		curMeasurement.aggregate(encsCost / revsCost);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return encs.isMeasurementvalid(w) && revs.isMeasurementvalid(w);
	}

}

