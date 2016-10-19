package ibe.measure;

import costs.DoubleReplaceCost;
import base.Measurable;
import base.Measure;

public class FileRekeyPerPermRevoked extends Measure {
	Measure rekeys = new FileRekeyRevP();
	Measure revs = new PermsRevoked();

	public FileRekeyPerPermRevoked() {
		curMeasurement = new DoubleReplaceCost();
	}

	@Override
	public String getPrintFriendlyName() {
		return "File rekeys per permission revocation";
	}

	@Override
	public void postExecMeasurement(Measurable w) {
        String type = Util.identify(w);

        // Check the implementation
        if(!type.equals("PT")) return;

		rekeys.postExecMeasurement(w);
		revs.postExecMeasurement(w);

		double rekeysCost = (Integer)rekeys.getCurCost();
		double revsCost = (Integer)revs.getCurCost();

		curMeasurement.aggregate(rekeysCost / revsCost);
	}

	@Override
	public boolean isMeasurementvalid(Measurable w) {
		return rekeys.isMeasurementvalid(w) && revs.isMeasurementvalid(w);
	}

}

