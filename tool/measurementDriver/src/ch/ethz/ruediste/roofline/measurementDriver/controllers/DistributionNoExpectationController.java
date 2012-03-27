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

public abstract class DistributionNoExpectationController<TQuantity extends Quantity<TQuantity>>
		extends DistributionControllerBase<TQuantity> {

	/**
	 * Fills the values of an error plot from a values plot. Shown is the
	 * deviation from the minimum in each series
	 */
	public void fillErrorPlot(DistributionPlot valuesPlot,
			DistributionPlot errorPlot) {
		// iterate over all series
		for (DistributionPlotSeries series : valuesPlot.getAllSeries()) {
			// iterate over all distributions in the series
			for (Entry<Long, DescriptiveStatistics> entry : series
					.getStatisticsMap().entrySet()) {

				// get the minimum of teh current distribution
				double min = entry.getValue().getMin();

				// iterate over all values in the distribution
				for (double value : entry.getValue().getValues()) {
					// add the value to the error plot
					errorPlot.addValue(series.getName(), entry.getKey(),
							toError(value / min));
				}
			}
		}
	}

	/**
	 * fills a distribution into a distribution plot
	 */
	@Override
	public void fillDistributionPlots(
			String kernelName, double x, DistributionPlot plotValues,
			DistributionPlot plotMinValues, QuantityMap result,
			QuantityCalculator<TQuantity> calc) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (RunQuantityMap runOutput : result.getRunMaps()) {
			TQuantity actual = runOutput.get(calc);

			addedValue(kernelName, x, actual);
			plotValues.addValue(kernelName, (long) x, actual.getValue());

			stats.addValue(actual.getValue());
			if (stats.getN() >= 10) {
				plotMinValues
						.addValue(kernelName, (long) x, stats.getMin());
				stats.clear();
			}
		}
	}

	protected void addedValue(String kernelName, double x, TQuantity actual) {
	}

	@Override
	protected double getX(KernelBase kernel, long problemSize) {
		return problemSize;
	}
}