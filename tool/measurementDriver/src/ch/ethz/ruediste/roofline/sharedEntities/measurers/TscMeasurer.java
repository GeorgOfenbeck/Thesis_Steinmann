package ch.ethz.ruediste.roofline.sharedEntities.measurers;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryAction;
import ch.ethz.ruediste.roofline.sharedEntities.*;

public class TscMeasurer extends TscMeasurerData implements
		IMeasurer<TscMeasurerOutput> {

	/**
	 * creates statistics of all event counts in the given measurement result
	 */
	public DescriptiveStatistics getStatistics(MeasurementResult result) {
		final DescriptiveStatistics statistics = new DescriptiveStatistics();

		addValues(result, new IUnaryAction<Double>() {
			public void apply(Double v) {
				statistics.addValue(v);
			}
		});

		return statistics;
	}

	public void addValues(MeasurementResult result,
			IUnaryAction<Double> addValue) {
		// iterate over all outputs
		for (TscMeasurerOutput output : result.getMeasurerOutputs(this)) {
			// scale the raw count
			addValue.apply(output.getTics().doubleValue());
		}

	}

	public void validate(TscMeasurerOutput output,
			MeasurementResult measurementResult) {
		// TODO Auto-generated method stub

	}
}
