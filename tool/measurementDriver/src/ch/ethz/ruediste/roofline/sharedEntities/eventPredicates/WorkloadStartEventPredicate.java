package ch.ethz.ruediste.roofline.sharedEntities.eventPredicates;

import ch.ethz.ruediste.roofline.sharedEntities.Workload;

public class WorkloadStartEventPredicate extends
		WorkloadStartEventPredicateData {

	public WorkloadStartEventPredicate() {
		super();
	}

	public WorkloadStartEventPredicate(Workload workload) {
		this();
		setWorkload(workload);
	}
}
