package ibe.measure;

import base.Implementation;
import base.Measurable;

import ibe.ImplementRbac0WInPtIbe;
import ibe.ImplementRbac0WInPubKey;
import ibe.ImplementRbac0WInSymKey;
import ibe.ImplementRbac0WInSymKeyDel;

public class Util {

    protected Util() {
        throw new UnsupportedOperationException();
    }

    // Figure out what type of IBE we're dealing with
    public static String identify(Measurable w) {
        String impl = (String)w.getCurMeasure(Implementation.nameProperty);
		if(impl.equals(ImplementRbac0WInPtIbe.class.getName())) {
            return "PT";
		} else if(impl.equals(ImplementRbac0WInPubKey.class.getName())) {
            return "PK";
		} else if(impl.equals(ImplementRbac0WInSymKey.class.getName())) {
            return "SK";
		} else if(impl.equals(ImplementRbac0WInSymKeyDel.class.getName())) {
            return "SD";
        } else {
            return "UN";
        }
    }

}

