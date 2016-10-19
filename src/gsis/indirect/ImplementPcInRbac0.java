package gsis.indirect;

import converters.Rbac1Converter;
import gsis.ImplementPcInRbac1;
import gsis.ImplementRbac1InRbac0;
import base.ImplementationComposition;

public class ImplementPcInRbac0 extends ImplementationComposition {

	public ImplementPcInRbac0() {
		supImpl = new ImplementPcInRbac1();
		subImpl = new ImplementRbac1InRbac0();
		converter = new Rbac1Converter();
	}

	public static String schemeName() {
		return "Rbac0";
	}
}
