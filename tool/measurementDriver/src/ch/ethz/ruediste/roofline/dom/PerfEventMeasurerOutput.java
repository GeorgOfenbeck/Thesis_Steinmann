package ch.ethz.ruediste.roofline.dom;

import java.io.PrintStream;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.statistics.IAddValue;

public class PerfEventMeasurerOutput extends PerfEventMeasurerOutputData {
	/** get the event count with the given name */
	public PerfEventCount getEventCount(String name) {
		for (PerfEventCount count : getEventCounts()) {
			if (count.getDefinition().getName().equals(name)) {
				return count;
			}
		}
		throw new Error("no event count for event named <" + name + "> found!");
	}

	/**
	 * creates statistics of all event counts in the given measurement result
	 */
	public static DescriptiveStatistics getStatistics(String name,
			MeasurementResult result) {
		final DescriptiveStatistics statistics = new DescriptiveStatistics();

		addValues(name, result, statistics);

		return statistics;
	}

	/**
	 * prints a raw value dump into the specified stream
	 */
	public static void printRaw(String name,
			MeasurementResult result, PrintStream out) {
		out.printf("Event: %s, <raw> <enabled> <running> <scaled>\n", name);

		// iterate over all outputs
		for (MeasurerOutputBase outputBase : result.getOutputs()) {
			// check if the output comes from the PerfEvent measurer
			if (outputBase instanceof PerfEventMeasurerOutput) {
				PerfEventMeasurerOutput output = (PerfEventMeasurerOutput) outputBase;
				PerfEventCount count = output.getEventCount(name);

				out.printf("%s %s %s %g\n", count.getRawCount(),
						count.getTimeEnabled(), count.getTimeRunning(),
						count.getScaledCount());
			}
		}
	}

	public static void addValues(String name, MeasurementResult result,
			final DescriptiveStatistics statistics) {
		addValues(name, result, new IAddValue() {
			public void addValue(double v) {
				statistics.addValue(v);
			}
		});
	}

	public static void addValues(String name,
			MeasurementResult result, IAddValue addValue) {
		// iterate over all outputs
		for (MeasurerOutputBase outputBase : result.getOutputs()) {
			// check if the output comes from the PerfEvent measurer
			if (outputBase instanceof PerfEventMeasurerOutput) {
				PerfEventMeasurerOutput output = (PerfEventMeasurerOutput) outputBase;
				PerfEventCount count = output.getEventCount(name);

				// scale the raw count
				addValue.addValue(
						count.getScaledCount());
			}
		}
	}
}
