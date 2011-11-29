package ch.ethz.ruediste.roofline.dom;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.statistics.IAddValue;

public class ExecutionTimeMeasurerOutput extends
		ExecutionTimeMeasurerOutputData {
	/**
	 * creates statistics of all event counts in the given measurement result
	 */
	public static DescriptiveStatistics getStatistics(MeasurementResult result) {
		final DescriptiveStatistics statistics = new DescriptiveStatistics();

		addValues(result, new IAddValue() {
			@Override
			public void addValue(double v) {
				statistics.addValue(v);
			}
		});

		return statistics;
	}

	public static void addValues(MeasurementResult result, IAddValue addValue) {
		// iterate over all outputs
		for (MeasurerOutputBase outputBase : result.getOutputs()) {
			// check if the output comes from the PerfEvent measurer
			if (outputBase instanceof ExecutionTimeMeasurerOutput) {
				ExecutionTimeMeasurerOutput output = (ExecutionTimeMeasurerOutput) outputBase;

				// scale the raw count
				addValue.addValue(output.getUSecs());
			}
		}

	}
}
