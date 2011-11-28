package ch.ethz.ruediste.roofline.dom;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class PerfEventMeasurerOutput extends PerfEventMeasurerOutputData {
	/** get the event count with the given name */
	public PerfEventCount getEventCount(String name) {
		for (PerfEventCount count : getEventCounts()) {
			if (count.getDefinition().getName().equals(name)) {
				return count;
			}
		}
		return null;
	}

	/**
	 * creates statistics of all event counts in the given measurement result
	 */
	public static DescriptiveStatistics getStatistics(String name,
			MeasurementResult result) {
		DescriptiveStatistics statistics = new DescriptiveStatistics();

		// iterate over all outputs
		for (MeasurerOutputBase outputBase : result.getOutputs()) {
			// check if the output comes from the PerfEvent measurer
			if (outputBase instanceof PerfEventMeasurerOutput) {
				PerfEventMeasurerOutput output = (PerfEventMeasurerOutput) outputBase;
				PerfEventCount count = output.getEventCount(name);

				// scale the raw count
				statistics.addValue(
						count.getRawCount().doubleValue()
								* count.getTimeEnabled().doubleValue()
								/ count.getTimeRunning().doubleValue());
			}
		}

		return statistics;
	}
}
