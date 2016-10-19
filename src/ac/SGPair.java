package ac;


public class SGPair {
	public String subject;
	public String group;

	@Override
	public String toString() {
		return "(" + subject + ", " + group + ")";
	}

	public SGPair(String subject, String group) {
		this.subject = subject;
		this.group = group;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
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
		SGPair other = (SGPair) obj;
		if(group == null) {
			if(other.group != null)
				return false;
		} else if(!group.equals(other.group))
			return false;
		if(subject == null) {
			if(other.subject != null)
				return false;
		} else if(!subject.equals(other.subject))
			return false;
		return true;
	}


}
