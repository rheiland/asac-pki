package base;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class represents the various properties that can be measured in Measurable objects.
 *
 */
public class Property {
	// The qualifier is the set of strings which describe who can answer
	// this property. A measurable's set of qualifiers (its class,
	// superclass, etc.) must include all of the qualifiers of a property in
	// order to answer the property.
	public HashSet<String> qualifiers = new HashSet<String>();
	public ArrayList<String> params = new ArrayList<String>();
	public String name;

	public String toString() {
		String ret = "";
		for(String q : qualifiers) {
			ret += "[" + q + "] ";
		}
		ret += name;
		ret += ":";
		for(String p : params) {
			ret += " {" + p + "}";
		}
		return ret;
	}

	public Property(String[] qualifiers, String name, String[] params) {
		for(String q : qualifiers) {
			this.qualifiers.add(q);
		}
		for(String p : params) {
			this.params.add(p);
		}
		this.name = name;
	}

	public Property(Object o, String name, String[] params) {
		addClassNameToQualifier(o.getClass());
		for(String p : params) {
			this.params.add(p);
		}
		this.name = name;
	}

	public Property(Object o, String name) {
		addClassNameToQualifier(o.getClass());
		this.name = name;
	}

	public Property(Class c, String name, String[] params) {
		addClassNameToQualifier(c);
		for(String p : params) {
			this.params.add(p);
		}
		this.name = name;
	}

	public Property(Class c, String name) {
		addClassNameToQualifier(c);
		this.name = name;
	}

	/**
	 * Add all class names of c to qualifier set.
	 * @param c
	 */
	public void addClassNameToQualifier(Class c) {
		if(c == null) {
			return;
		}
		qualifiers.add(c.getName());
		addClassNameToQualifier(c.getSuperclass());
	}

	/**
	 * Does the property being queried matches this property? This is used
	 * in each measurable class (in isMeasurable and getCurMeasure) to test
	 * whether the property being queried matches an answerable property.
	 * @param other the property being queried by the measurable.
	 * @return true if all qualifiers being queried are currently in the set
	 * of qualifiers for the built-in property (but not necessarilly the
	 * other way around) and the property names match
	 */
	public boolean match(Property other) {
		if(!other.name.equals(name)) return false;
		return matchQualifier(other);
	}

	/**
	 * Only check the qualifier and not the name.
	 * Useful in the beginning of isMeasurable property.
	 * @param other the property being queried by the measurable.
	 * @return true if all qualifiers being queried are currently in the set
	 * of qualifiers for the built-in property (but not necessarilly the
	 * other way around)
	 */
	public boolean matchQualifier(Property other) {
		return qualifiers.containsAll(other.qualifiers);
	}
}
