package ch.ethz.ruediste.roofline.sharedEntities.eventPredicates;

public class MeasurementRunEventPredicate extends
		MeasurementRunEventPredicateData {
	public enum MeasurementRunEventEnum {
		Start,
		Stop,
	};

	private MeasurementRunEventEnum event;

	public MeasurementRunEventPredicate() {
	}

	public MeasurementRunEventPredicate(MeasurementRunEventEnum event) {
		this();
		this.event = event;
	}

	public MeasurementRunEventEnum getEvent() {
		return event;
	}

	public void setEvent(MeasurementRunEventEnum event) {
		this.event = event;
	}

	@Override
	public int getEventNr() {
		return event.ordinal();
	}
}
