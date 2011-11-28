package ch.ethz.ruediste.roofline.dom;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class ExecutionTimeMeasurerOutput extends
		ExecutionTimeMeasurerOutputData {
	/**
	 * creates statistics of all event counts in the given measurement result
	 */
	public static DescriptiveStatistics getStatistics(MeasurementResult result) {
		DescriptiveStatistics statistics = new DescriptiveStatistics();

		// iterate over all outputs
		for (MeasurerOutputBase outputBase : result.getOutputs()) {
			// check if the output comes from the PerfEvent measurer
			if (outputBase instanceof ExecutionTimeMeasurerOutput) {
				ExecutionTimeMeasurerOutput output = (ExecutionTimeMeasurerOutput) outputBase;

				// scale the raw count
				statistics.addValue(output.getUSecs());
			}
		}

		return statistics;
	}
}
