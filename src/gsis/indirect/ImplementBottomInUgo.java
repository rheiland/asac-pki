package gsis.indirect;

import converters.Rbac0Converter;
import gsis.ImplementRbac0InUgo;
import base.ImplementationComposition;

public class ImplementBottomInUgo extends ImplementationComposition {

	public ImplementBottomInUgo() {
		supImpl = new ImplementBottomInRbac0();
		subImpl = new ImplementRbac0InUgo();
		converter = new Rbac0Converter();
	}

	public static String schemeName() {
		return "ugo";
	}
}
