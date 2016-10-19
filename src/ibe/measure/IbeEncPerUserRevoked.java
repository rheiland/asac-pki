package ibe.measure;

import costs.DoubleReplaceCost;
import base.Measurable;
import base.Measure;

public class IbeEncPerUserRevoked extends Measure {
	Measure encs = new IbeEncRevU();
	Measure revs = new UsersRevoked();

	public IbeEncPerUserRevoked() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getPrintFriendlyName() {
		return "IBE encryptions per user revocation";
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

