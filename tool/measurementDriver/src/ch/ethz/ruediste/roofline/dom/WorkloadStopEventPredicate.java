package ch.ethz.ruediste.roofline.dom;

public class WorkloadStopEventPredicate extends WorkloadStopEventPredicateData {
	private Workload workload;

	public WorkloadStopEventPredicate() {
		super();
	}

	public WorkloadStopEventPredicate(Workload workload) {
		this();
		this.workload = workload;
	}

	@Override
	public int getWorkloadId() {
		return workload.getId();
	}

	public Workload getWorkload() {
		return workload;
	}

	public void setWorkload(Workload workload) {
		this.workload = workload;
	}
}
