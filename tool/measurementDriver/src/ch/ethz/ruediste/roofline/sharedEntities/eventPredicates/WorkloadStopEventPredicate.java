package ch.ethz.ruediste.roofline.sharedEntities.eventPredicates;

import java.util.Set;

import ch.ethz.ruediste.roofline.sharedEntities.Workload;

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

	@Override
	public void addAll(Set<Object> result) {
		super.addAll(result);
		if (getWorkload() != null) {
			getWorkload().addAll(result);
		}
	}
}
