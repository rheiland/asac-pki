package gsis.indirect;

import converters.Rbac0Converter;
import gsis.ImplementRbac0InUgo;
import gsis.ImplementRoleLikeInRbac0;
import base.ImplementationComposition;

public class ImplementRoleLikeInUgo extends ImplementationComposition {

	public ImplementRoleLikeInUgo() {
		supImpl = new ImplementRoleLikeInRbac0();
		subImpl = new ImplementRbac0InUgo();
		converter = new Rbac0Converter();
	}

	public static String schemeName() {
		return "ugo";
	}
}
