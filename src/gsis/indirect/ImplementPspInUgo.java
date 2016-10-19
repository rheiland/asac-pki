package gsis.indirect;

import gsis.ImplementRbac0InUgo;
import converters.Rbac0Converter;
import base.ImplementationComposition;

public class ImplementPspInUgo extends ImplementationComposition {

	public ImplementPspInUgo() {
		supImpl = new ImplementPspInRbac0();
		subImpl = new ImplementRbac0InUgo();
		converter = new Rbac0Converter();
	}

	public static String schemeName() {
		return "ugo";
	}
}
