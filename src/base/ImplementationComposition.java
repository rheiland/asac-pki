package base;

import java.util.ArrayList;

public class ImplementationComposition extends Implementation {
	//supImpl.wScheme -> supImpl.scheme -> subImpl.scheme
	protected Implementation supImpl, subImpl;
	protected StateToWorkloadStateConverter converter;

	public ImplementationComposition() {}

	public ImplementationComposition(Implementation supImpl, Implementation subImpl, StateToWorkloadStateConverter converter) {
		this.supImpl = supImpl;
		this.subImpl = subImpl;
		this.converter = converter;
	}

	@Override
	public void init(WorkloadState ws) {
		supImpl.init(ws);
		subImpl.init(converter.toWorkloadState(supImpl.scheme.state));
		this.wScheme = supImpl.wScheme;
		this.scheme = subImpl.scheme;
	}

	@Override
	public State stateMap(WorkloadState ws) {
		return null;
	}

	@Override
	public void action(Action a) {
		supImpl.action(a);
		ArrayList<Action> actionsExecuted = (ArrayList<Action>) supImpl.getCurMeasure(new
		                                    Property(Scheme.class, Scheme.Q_ACTIONSEXECUTED, new String[0]));
		boolean noReset = false;
		for(Action aa : actionsExecuted) {
			subImpl.scheme.noReset = noReset;
			subImpl.wScheme.noReset = noReset;
			subImpl.action(aa);
			noReset = true;
		}
	}

}
