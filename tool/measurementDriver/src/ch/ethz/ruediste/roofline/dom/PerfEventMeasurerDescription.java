package ch.ethz.ruediste.roofline.dom;

public class PerfEventMeasurerDescription extends
		PerfEventMeasurerDescriptionData {
	public void addEvent(String name, String definition) {
		PerfEventDefinition def = new PerfEventDefinition();
		def.setName(name);
		def.setDefinition(definition);
		getEvents().add(def);
	}
}
