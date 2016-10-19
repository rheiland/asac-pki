package base;

import static base.Implementation.numSubs;

import java.util.Collection;

public class IAccessACH extends IAccessAC<String>{

	public IAccessACH(Collection<String> obj, State s, int factor) {
		super(obj, s, factor);
	}
	
	@Override
	public String next() {
		String ret = it.next();
		s.incrementAccessIO(numSubs(ret) + factor, 0);
		return ret;
	}
	

}
