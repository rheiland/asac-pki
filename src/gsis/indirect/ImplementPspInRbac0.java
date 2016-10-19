package gsis.indirect;

import gsis.ImplementPspInRbac1;
import gsis.ImplementRbac1InRbac0;
import converters.Rbac1Converter;
import base.ImplementationComposition;

public class ImplementPspInRbac0 extends ImplementationComposition {

	public ImplementPspInRbac0() {
		supImpl = new ImplementPspInRbac1();
		subImpl = new ImplementRbac1InRbac0();
		converter = new Rbac1Converter();
	}

	public static String schemeName() {
		return "Rbac0";
	}
}
