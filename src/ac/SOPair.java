package ac;

/**
 * Subject-object pair used in some AC states.
 * @author yechen
 *
 */
public class SOPair {
	public String subject;
	public String object;

	@Override
	public String toString() {
		return "(" + subject + "," + object + ")";
	}

	public SOPair(String s, String o) {
		subject = s;
		object = o;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		SOPair other = (SOPair) obj;
		if(object == null) {
			if(other.object != null)
				return false;
		} else if(!object.equals(other.object))
			return false;
		if(subject == null) {
			if(other.subject != null)
				return false;
		} else if(!subject.equals(other.subject))
			return false;
		return true;
	}


}
