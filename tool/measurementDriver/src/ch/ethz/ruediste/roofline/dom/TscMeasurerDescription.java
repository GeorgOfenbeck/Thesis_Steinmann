package ch.ethz.ruediste.roofline.dom;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.statistics.IAddValue;

public class TscMeasurerDescription extends TscMeasurerDescriptionData
		implements
		IMeasurerDescription<TscMeasurerOutput> {

	/**
	 * creates statistics of all event counts in the given measurement result
	 */
	public DescriptiveStatistics getStatistics(MeasurementResult result) {
		final DescriptiveStatistics statistics = new DescriptiveStatistics();

		addValues(result, new IAddValue() {
			public void addValue(double v) {
				statistics.addValue(v);
			}
		});

		return statistics;
	}

	public void addValues(MeasurementResult result, IAddValue addValue) {
		// iterate over all outputs
		for (TscMeasurerOutput output : result.getMeasurerOutputs(this)) {
			// scale the raw count
			addValue.addValue(output.getTics().doubleValue());
		}

	}
}