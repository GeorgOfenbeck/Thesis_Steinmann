package ch.ethz.ruediste.roofline.sharedEntities.eventPredicates;

import ch.ethz.ruediste.roofline.sharedEntities.Workload;

public class WorkloadStopEventPredicate extends WorkloadStopEventPredicateData {

	public WorkloadStopEventPredicate() {
		super();
	}

	public WorkloadStopEventPredicate(Workload workload) {
		this();
		setWorkload(workload);
	}
}
