package gsis;

public class OGPair {
	String object;
	String group;

	@Override
	public String toString() {
		return "(" + object + ", " + group + ")";
	}

	public OGPair(String object, String group) {
		this.object = object;
		this.group = group;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
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
		OGPair other = (OGPair) obj;
		if(group == null) {
			if(other.group != null)
				return false;
		} else if(!group.equals(other.group))
			return false;
		if(object == null) {
			if(other.object != null)
				return false;
		} else if(!object.equals(other.object))
			return false;
		return true;
	}




}
