package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.Range;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.DistributionPlot.DistributionPlotSeries;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.RunQuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.ArithmeticKernel.ArithmeticOperation;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MemoryKernel.MemoryOperation;

import com.google.inject.Inject;

public class ValidationMeasurementControllerBase {

	public static ConfigurationKey<Boolean> fastKey = ConfigurationKey.Create(
			Boolean.class, "fast", "reduce proble sizes", false);

	@Inject
	public Configuration configuration;

	public ValidationMeasurementControllerBase() {
		super();
	}

	/**
	 * @param space
	 * @param kernelNames
	 */
	public void setupMemoryKernels(ParameterSpace space,
			HashMap<KernelBase, String> kernelNames) {
		// setup read kernel
		{
			MemoryKernel kernel = new MemoryKernel();
			kernel.setUnroll(1);
			kernel.setDlp(1);
			kernel.setOptimization("-O3 -msse2");
			kernel.setPrefetchDistance(0L);
			kernel.setOperation(MemoryOperation.MemoryOperation_READ);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "Read");
		}

		// setup write kernel
		{
			MemoryKernel kernel = new MemoryKernel();
			kernel.setUnroll(2);
			kernel.setDlp(1);
			kernel.setOptimization("-O3");
			kernel.setPrefetchDistance(0L);
			kernel.setOperation(MemoryOperation.MemoryOperation_WRITE);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "Write");
		}

		// setup triad kernel
		{
			TriadKernel kernel = new TriadKernel();
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "Triad");
		}

		long maxSize = 16;
		if (configuration.get(fastKey)) {
			maxSize = 1;
		}

		// setup buffer sizes
		for (long i = 128; i < 1024 * 1024 * maxSize; i *= 2) {
			space.add(bufferSizeAxis, i);
		}
	}

	/**
	 * @param space
	 * @param kernelNames
	 */
	public void setupArithmeticKernels(ParameterSpace space,
			HashMap<KernelBase, String> kernelNames) {
		// setup add sse kernel
		{
			ArithmeticKernel kernel = new ArithmeticKernel();
			kernel.setUnroll(4);
			kernel.setDlp(2);
			kernel.setOptimization("-O3 -msse2");
			kernel.setOperation(ArithmeticOperation.ArithmeticOperation_ADD);
			kernel.setInstructionSet(InstructionSet.SSE);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "ADD SSE");
		}

		// setup add x87 kernel
		{
			ArithmeticKernel kernel = new ArithmeticKernel();
			kernel.setUnroll(4);
			kernel.setDlp(3);
			kernel.setOptimization("-O3");
			kernel.setOperation(ArithmeticOperation.ArithmeticOperation_ADD);
			kernel.setInstructionSet(InstructionSet.x87);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "ADD x87");
		}

		// setup mul sse kernel
		{
			ArithmeticKernel kernel = new ArithmeticKernel();
			kernel.setUnroll(4);
			kernel.setDlp(2);
			kernel.setOptimization("-O3 -msse2");
			kernel.setOperation(ArithmeticOperation.ArithmeticOperation_MUL);
			kernel.setInstructionSet(InstructionSet.SSE);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "MUL SSE");
		}

		// setup mul x87 kernel
		{
			ArithmeticKernel kernel = new ArithmeticKernel();
			kernel.setUnroll(4);
			kernel.setDlp(3);
			kernel.setOptimization("-O3");
			kernel.setOperation(ArithmeticOperation.ArithmeticOperation_MUL);
			kernel.setInstructionSet(InstructionSet.x87);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "MUL x87");
		}

		long maxSize = 128;
		if (configuration.get(fastKey)) {
			maxSize = 1;
		}

		// setup iteration counts
		for (long i = 128; i < 1024 * 1024 * maxSize; i *= 4) {
			space.add(iterationsAxis, i);
		}
	}

	double toError(double ratio) {
		if (ratio < 1)
			ratio = 1 / ratio;
		return 100 * (ratio - 1);
	}

	/**
	 * Fills the values of an error plot from a values plot.
	 */
	public void fillErrorPlotMin(DistributionPlot valuesPlot,
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

	public void fillErrorPlotExpected(DistributionPlot valuesPlot,
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
	 * fills a distribution into a distribution plot
	 */
	public <T extends Quantity<T>> void fillDistributionPlots(
			String kernelName, long expected, DistributionPlot plotValues,
			DistributionPlot plotMinValues, QuantityMap result,
			QuantityCalculator<T> calc) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (RunQuantityMap runOutput : result.getRunMaps()) {
			T actual = runOutput.get(calc);

			plotValues.addValue(kernelName, expected, actual.getValue());

			stats.addValue(actual.getValue());
			if (stats.getN() >= 10) {
				plotMinValues.addValue(kernelName, expected, stats.getMin());
				stats.clear();
			}
		}
	}

	/**
	 * fills a distribution into a distribution plot
	 */
	public <T extends Quantity<T>> void fillDistributionPlotsExpected(
			String kernelName, double expected, DistributionPlot plotValues,
			DistributionPlot plotMinValues, QuantityMap result,
			QuantityCalculator<T> calc) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (RunQuantityMap runOutput : result.getRunMaps()) {
			T actual = runOutput.get(calc);

			plotValues.addValue(kernelName, (long) expected, actual.getValue()
					/ expected);

			stats.addValue(actual.getValue());
			if (stats.getN() >= 10) {
				plotMinValues.addValue(kernelName, (long) expected,
						stats.getMin() / expected);
				stats.clear();
			}
		}
	}

	public Range<Double> yErrorRange() {
		return Range.between(0., 50.);
	}
}