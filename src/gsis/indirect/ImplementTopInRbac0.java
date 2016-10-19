package gsis.indirect;

import converters.Rbac1Converter;
import gsis.ImplementRbac1InRbac0;
import gsis.ImplementTopInRbac1;
import base.ImplementationComposition;

public class ImplementTopInRbac0 extends ImplementationComposition {

	public ImplementTopInRbac0() {
		supImpl = new ImplementTopInRbac1();
		subImpl = new ImplementRbac1InRbac0();
		converter = new Rbac1Converter();
	}

	public static String schemeName() {
		return "Rbac0";
	}
}
