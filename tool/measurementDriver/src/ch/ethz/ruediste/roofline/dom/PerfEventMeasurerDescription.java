package ch.ethz.ruediste.roofline.dom;

import java.io.PrintStream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.statistics.IAddValue;

public class PerfEventMeasurerDescription extends
		PerfEventMeasurerDescriptionData
		implements IMeasurerDescription<PerfEventMeasurerOutput> {

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

	/**
	 * prints a raw value dump into the specified stream
	 */
	public void printRaw(String name,
			MeasurementResult result, PrintStream out) {
		out.printf("Event: %s, <raw> <enabled> <running> <scaled>\n", name);

		// iterate over all outputs
		for (PerfEventMeasurerOutput output : result.getMeasurerOutputs(this)) {
			output.printRaw(name, out);
		}
	}

	public void addValues(String name,
			MeasurementResult result, IAddValue addValue) {
		// iterate over all outputs
		for (PerfEventMeasurerOutput output : result.getMeasurerOutputs(this)) {
			addValue.addValue(output
					.getEventCount(name).getScaledCount());
		}
	}

	public void addValues(String name, MeasurementResult result,
			final DescriptiveStatistics statistics) {
		addValues(name, result, new IAddValue() {
			public void addValue(double v) {
				statistics.addValue(v);
			}
		});
	}

	/**
	 * creates statistics of all event counts in the given measurement result
	 */
	public DescriptiveStatistics getStatistics(String name,
			MeasurementResult result) {
		final DescriptiveStatistics statistics = new DescriptiveStatistics();

		addValues(name, result, statistics);

		return statistics;
	}

}
