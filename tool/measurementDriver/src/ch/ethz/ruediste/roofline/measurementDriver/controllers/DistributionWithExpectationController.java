package ch.ethz.ruediste.roofline.measurementDriver.controllers;

import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.DistributionPlot.DistributionPlotSeries;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.RunQuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.KernelBase;

public abstract class DistributionWithExpectationController<TQuantity extends Quantity<TQuantity>>
		extends DistributionControllerBase<TQuantity> {

	/**
	 * fill an error plot. The expected value is taken from the x axis
	 */
	@Override
	public void fillErrorPlot(DistributionPlot valuesPlot,
			DistributionPlot errorPlot) {
		// iterate over all series
		for (DistributionPlotSeries series : valuesPlot.getAllSeries()) {
			// iterate over all distributions in the series
			for (Entry<Long, DescriptiveStatistics> entry : series
					.getStatisticsMap().entrySet()) {
				// iterate over all values in the distribution
				for (double value : entry.getValue().getValues()) {
					// add the value to the error plot
					errorPlot.addValue(series.getName(), entry.getKey(),
							toError(value));
				}
			}
		}
	}

	/**
	 * fills a distribution into a distribution plot. The values are divided by
	 * the expected value
	 */
	@Override
	public void fillDistributionPlots(
			String kernelName, double expected,
			DistributionPlot plotValues,
			DistributionPlot plotMinValues, QuantityMap result,
			QuantityCalculator<TQuantity> calc) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (RunQuantityMap runOutput : result.getRunMaps()) {
			TQuantity actual = runOutput.get(calc);

			addedValue(kernelName, expected, actual);

			plotValues.addValue(kernelName, (long) expected,
					actual.getValue()
							/ expected);

			stats.addValue(actual.getValue());
			if (stats.getN() >= 10) {
				plotMinValues.addValue(kernelName, (long) expected,
						stats.getMin() / expected);
				stats.clear();
			}
		}
	}

	protected void addedValue(String kernelName, double expected,
			TQuantity actual) {
	}

	@Override
	protected final double getX(KernelBase kernel, long problemSize) {
		return expected(kernel).getValue();
	}

	protected abstract TQuantity expected(KernelBase kernel);
}