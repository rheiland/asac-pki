package base;
import java.util.Arrays;

/**
 * An action can be either an action in the workload, workflow, or one in the AC scheme. Each action
 * can be either a command or a query (type).
 * Furthermore, each action must have a name and zero or more parameters.
 * By default, an action is not a query.
 * There's a constructor which can override this so that an action can be a query.
 *
 */
public class Action {
	public String name;
	public String[] params;
	/**
	 * Is it a query or command
	 */
	public boolean isQuery;

	public String toString() {
		String ret =  name + "(";
		for(String param : params) {
			ret += param + ", ";
		}
		ret = ret.substring(0, ret.length() - 2);
		ret += ")";
		return ret;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isQuery ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(params);
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
		Action other = (Action) obj;
		if(isQuery != other.isQuery)
			return false;
		if(name == null) {
			if(other.name != null)
				return false;
		} else if(!name.equals(other.name))
			return false;
		if(!Arrays.equals(params, other.params))
			return false;
		return true;
	}



	public Action(String name, String... params) {
		this.name = name;
		this.params = params;
		isQuery = false;
	}

	public Action(boolean isQuery, String name, String... params) {
		this.name = name;
		this.params = params;
		this.isQuery = isQuery;
	}




}
