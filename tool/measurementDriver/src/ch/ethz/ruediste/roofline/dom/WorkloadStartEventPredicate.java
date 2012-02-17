package ch.ethz.ruediste.roofline.dom;

public class WorkloadStartEventPredicate extends
		WorkloadStartEventPredicateData {

	private Workload workload;

	public WorkloadStartEventPredicate() {
		super();
	}

	public WorkloadStartEventPredicate(Workload workload) {
		this();
		this.workload = workload;
	}

	public Workload getWorkload() {
		return workload;
	}

	public void setWorkload(Workload workload) {
		this.workload = workload;
	}

	@Override
	public int getWorkloadId() {
		return workload.getId();
	}
}
