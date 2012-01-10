package ch.ethz.ruediste.roofline.dom;

import org.apache.commons.lang3.tuple.Pair;

public class PerfEventMeasurerDescription extends
		PerfEventMeasurerDescriptionData {

	public PerfEventMeasurerDescription() {
	}

	public PerfEventMeasurerDescription(String name, String definition) {
		addEvent(name, definition);
	}

	public PerfEventMeasurerDescription(Pair<String, String>... events) {
		for (Pair<String, String> event : events) {
			addEvent(event.getLeft(), event.getRight());
		}
	}

	public void addEvent(String name, String definition) {
		PerfEventDefinition def = new PerfEventDefinition();
		def.setName(name);
		def.setDefinition(definition);
		getEvents().add(def);
	}
}
