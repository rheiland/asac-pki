package skel.measure;

import base.Implementation;
import base.Measurable;

//import skel.ImplementPkiWInCrl;
//import skel.ImplementPkiWInOcsp;
//import skel.ImplementPkiWInCertTransp;
import skel.ImplementSkelWInModel1;

public class Util {

    protected Util() {
        throw new UnsupportedOperationException();
    }

//    // Figure out what type of IBE we're dealing with
    // Figure out what type of Skeleton we're dealing with
    public static String identify(Measurable w) {
        String impl = (String)w.getCurMeasure(Implementation.nameProperty);
		if(impl.equals(ImplementSkelWInModel1.class.getName())) {
            return "SKEL";
//		if(impl.equals(ImplementPkiWInOcsp.class.getName())) {
//            return "OC";
//		} else if(impl.equals(ImplementPkiWInCertTransp.class.getName())) {
//            return "CT";
        } else {
            return "UN";
        }
    }

}

