package ch.ethz.ruediste.roofline.sharedEntities.eventPredicates;

public class ThreadEventPredicate extends ThreadEventPredicateData {
	enum ThreadEventEnum {
		Started,
		Exiting,
	};

	private ThreadEventEnum event;

	public ThreadEventPredicate() {

	}

	public ThreadEventPredicate(ThreadEventEnum event) {
		this();
		this.event = event;
	}

	public ThreadEventEnum getEvent() {
		return event;
	}

	public void setEvent(ThreadEventEnum event) {
		this.event = event;
	}

	@Override
	public int getEventNr() {
		return event.ordinal();
	}
}
