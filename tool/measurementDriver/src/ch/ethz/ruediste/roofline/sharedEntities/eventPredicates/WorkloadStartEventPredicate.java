package ch.ethz.ruediste.roofline.sharedEntities.eventPredicates;

import java.util.Set;

import ch.ethz.ruediste.roofline.sharedEntities.Workload;

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

	@Override
	public void addAll(Set<Object> result) {
		super.addAll(result);
		if (getWorkload() != null) {
			getWorkload().addAll(result);
		}
	}
}
