package ch.ethz.ruediste.roofline.sharedEntities.eventPredicates;

import ch.ethz.ruediste.roofline.sharedEntities.Workload;

public class WorkloadEventPredicate extends
		WorkloadEventPredicateData {

	public enum WorkloadEventEnum {
		Start,
		Stop,
		KernelStart,
		KernelStop,
	}

	private WorkloadEventEnum event;

	public WorkloadEventPredicate() {
		super();
	}

	public WorkloadEventPredicate(Workload workload, WorkloadEventEnum event) {
		this();
		setWorkload(workload);
		setEvent(event);
	}

	@Override
	public int getEventNr() {
		return event.ordinal();
	}

	public WorkloadEventEnum getEvent() {
		return event;
	}

	public void setEvent(WorkloadEventEnum event) {
		this.event = event;
	}
}
