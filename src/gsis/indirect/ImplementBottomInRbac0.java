package gsis.indirect;

import converters.Rbac1Converter;
import gsis.ImplementBottomInRbac1;
import gsis.ImplementRbac1InRbac0;
import base.ImplementationComposition;

public class ImplementBottomInRbac0 extends ImplementationComposition {

	public ImplementBottomInRbac0() {
		supImpl = new ImplementBottomInRbac1();
		subImpl = new ImplementRbac1InRbac0();
		converter = new Rbac1Converter();
	}

	public static String schemeName() {
		return "Rbac0";
	}
}
