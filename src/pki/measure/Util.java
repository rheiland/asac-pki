package pki.measure;

import base.Implementation;
import base.Measurable;

//import skel.ImplementPkiWInCrl;
//import skel.ImplementPkiWInOcsp;
//import skel.ImplementPkiWInCertTransp;
import pki.ImplementPkiWInCrl;
import pki.ImplementPkiWInOcsp;

public class Util {

    protected Util() {
        throw new UnsupportedOperationException();
    }

//    // Figure out what type of IBE we're dealing with
    // Figure out what type of Skeleton we're dealing with
    public static String identify(Measurable w) {
        String impl = (String)w.getCurMeasure(Implementation.nameProperty);
//		if(impl.equals(ImplementSkelWInModel1.class.getName())) {
		if(impl.equals(ImplementPkiWInCrl.class.getName())) {
            return "CRL";
		} else if(impl.equals(ImplementPkiWInOcsp.class.getName())) {
            return "OCSP";
//		} else if(impl.equals(ImplementPkiWInCT.class.getName())) {
 //           return "CT";
        } else {
            return "UN";
        }
    }

}