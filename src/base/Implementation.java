package base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation is responsible for bridging the simulating scheme with the
 * simulated scheme.
 * @author yechen
 *
 */
public abstract class Implementation implements Measurable {
	protected Scheme scheme;
	public WorkloadScheme wScheme;
	public System sys;
	public static final String Q_STATEMAP="StateMap", Q_COMMANDMAP = "CommandMap", Q_IMPL_NAME = "ImplementationName";
	public static boolean accountIO = false;
	private HashMap<String, HashSet<String>> nonHomomorphicConcats = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> nonHomomorphicConcatsExt = new HashMap<String, HashSet<String>>();
	public static final Property nameProperty = new Property(Implementation.class, Q_IMPL_NAME, new String[0]);

	/**
	 * Non-homomorphic concat sentinel.
	 * @return
	 */
	public static String sentinel() {
		return "_@_";
	}

	public static String schemeName() {
		return "Unnamed scheme";
	}

	public void removeStrCat(String ... strs) {
		String ret = "";
		for(String s : strs) {
			ret += sentinel() + s;

		}
		ret = ret.substring(sentinel().length());

		if(nonHomomorphicConcats.containsKey(ret))
			nonHomomorphicConcats.remove(ret);
	}

	public void removeStrCatExt(String ... strs) {
		String ret = "";
		for(String s : strs) {
			ret += sentinel() + s;

		}
		ret = ret.substring(sentinel().length());

		if(nonHomomorphicConcatsExt.containsKey(ret)) {
			nonHomomorphicConcats.remove(ret);
			nonHomomorphicConcatsExt.remove(ret);
		}

	}

	/**
	 * Used to concatenate non-homomorphic strings.
	 * @param strs
	 * @return concatenated strings
	 */
	public String strCat(String ... strs) {
		String ret = "";
		for(String s : strs) {
			ret += sentinel() + s;

		}
		ret = ret.substring(sentinel().length());

		if(nonHomomorphicConcats.containsKey(ret))
			return ret;
		nonHomomorphicConcats.put(ret, new HashSet<String>());
		for(String s : strs) {
			if(nonHomomorphicConcats.containsKey(s)) {
				nonHomomorphicConcats.get(ret).addAll(nonHomomorphicConcats.get(s));
			} else {
				nonHomomorphicConcats.put(ret, new HashSet<String>());
				nonHomomorphicConcats.get(ret).add(s);
			}
		}
		return ret;
	}

	/**
	 * Concatenate and is intended for Aux Machine
	 * @param strs
	 * @return
	 */
	public String strCatExt(String ... strs) {
		String ret = "";
		for(String s : strs) {
			ret += sentinel() + s;
		}
		ret = ret.substring(sentinel().length());

		if(nonHomomorphicConcatsExt.containsKey(ret)) {
			return ret;
		}
		nonHomomorphicConcats.put(ret, new HashSet<String>());
		nonHomomorphicConcatsExt.put(ret, new HashSet<String>());
		for(String s : strs) {
			if(nonHomomorphicConcats.containsKey(s)) {
				nonHomomorphicConcats.get(ret).addAll(nonHomomorphicConcats.get(s));
				nonHomomorphicConcatsExt.get(ret).addAll(nonHomomorphicConcatsExt.get(s));
			} else {
				nonHomomorphicConcats.put(ret, new HashSet<String>());
				nonHomomorphicConcatsExt.put(ret, new HashSet<String>());
				nonHomomorphicConcats.get(ret).add(s);
				nonHomomorphicConcatsExt.get(ret).add(s);
			}
		}
		return ret;
	}

	/**
	 * Executes the AC actions mapped to the given workload action.
	 * We need to execute the action in both wsScheme as well as in scheme.
	 * @param a the workload action to be executed.
	 */
	public void action(Action a) {
		scheme.newWorkloadCommand();
		wScheme.action(a);
	}
	/**
	 * Initializes wsScheme and scheme given a particular workload state.
	 * @param ws the workload state we need to init to. (Do it in both
	 * scheme and wsScheme)
	 */
	public abstract void init(WorkloadState ws);

	public static int numSubs(String main) {
		return 1 + (main.length() - main.replace(sentinel(), "").length()) / sentinel().length();
	}

	/**
	 * This is the state-state map.
	 * @param ws the workload state
	 * @return the ac state which is ~ to ws.
	 */
	public abstract State stateMap(WorkloadState ws);

	/**
	 * Given action a and its context w, fill in the parameters based on Workload Scheme state and w.
	 * This is the delegate.
	 * @param a
	 * @param w
	 * @return the new action with missing parameters filled in.
	 */
	public Action addParameters(Action a, Workflow w) {
		return wScheme.addParameters(a, w);
	}

	@Override
	public Set<Property> getMeasurables() {
		HashSet<Property> ret = new HashSet<Property>();
		ret.addAll(scheme.getMeasurables());
		ret.addAll(wScheme.getMeasurables());
		ret.add(nameProperty);
		return ret;
	}

	@Override
	public boolean isMeasurable(Property p) {
		Set<Property> props = getMeasurables();
		for(Property pp : props) {
			if(pp.match(p)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getCurMeasure(Property p) {
		Property qTest = new Property(this, "__test");
		if(p.name.equals(Q_IMPL_NAME)) {
			return this.getClass().getName();
		} else if(!qTest.matchQualifier(p)) {
			if(scheme.isMeasurable(p)) {
				return scheme.getCurMeasure(p);
			} else if(wScheme.isMeasurable(p)) {
				return wScheme.getCurMeasure(p);
			}
		}
		throw new InvalidMeasureException(p);
	}


}
