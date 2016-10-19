package base;

/**
 * This interface provides the abstract handle for a converter to be able to
 * convert an AC state to a workload state.
 * @author yechen
 *
 */
public interface StateToWorkloadStateConverter {
	public WorkloadState toWorkloadState(State in);
}
