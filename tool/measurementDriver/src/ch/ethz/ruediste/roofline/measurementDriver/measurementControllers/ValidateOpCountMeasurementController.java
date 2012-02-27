package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.entities.Axes.kernelAxis;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.DistributionPlot.DistributionPlotSeries;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;

import com.google.inject.Inject;

public class ValidateOpCountMeasurementController extends
		ValidationMeasurementControllerBase implements IMeasurementController {

	public String getName() {
		return "valOp";
	}

	public String getDescription() {
		return "runs validation measurements of the operation count time";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	@Inject
	MeasurementService measurementService;

	@Inject
	PlotService plotService;

	public void measure(String outputName) throws IOException {
		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();
		ParameterSpace space = new ParameterSpace();

		setupArithmeticKernels(space, kernelNames);

		// initialize plot
		DistributionPlot plotValues = new DistributionPlot();
		plotValues.setOutputName(outputName + "Values");
		plotValues.setTitle("OpCount Values");

		DistributionPlot plotError = new DistributionPlot();
		plotError.setOutputName(outputName + "Error");
		plotError.setTitle("OpCount Error");

		// iterate over space
		for (Coordinate coordinate : space) {
			// initialize the kernel
			final KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			// get the calculator for the op count
			QuantityCalculator<OperationCount> calc = quantityMeasuringService
					.getOperationCountCalculator(kernel.getSuggestedOperation());

			IMeasurementBuilder builder = new IMeasurementBuilder() {

				public Measurement build(Map<String, MeasurerSet> sets) {

					// setup the measurement
					Measurement measurement = new Measurement();
					Workload workload = new Workload();
					measurement.addWorkload(workload);
					workload.setKernel(kernel);
					workload.setMeasurerSet(sets.get("operations"));

					return measurement;
				}
			};

			QuantityMap quantities = quantityMeasuringService
					.getQuantities(builder, 10).with("operations", calc).get();

			OperationCount expected = kernel.getExpectedOperationCount();

			for (OperationCount actual : quantities.get(calc)) {

				double ratio = actual.getValue() / expected.getValue();
				//System.out.printf("%s -> %g\n", actual, ratio);

				plotValues.addValue(kernelNames.get(kernel),
						(long) expected.getValue(),
						//coordinate.get(bufferSizeAxis),
						ratio);

				if (ratio < 1) {
					ratio = 1 / ratio;
				}
				plotError.addValue(kernelNames.get(kernel),
						(long) expected.getValue(),
						//coordinate.get(bufferSizeAxis), 
						100 * (ratio - 1));

			}
		}

		SeriesPlot plotMinValues = new SeriesPlot();
		plotMinValues.setOutputName(outputName + "MinValues");
		plotMinValues.setTitle("OpCount Min Values");

		SeriesPlot plotMinError = new SeriesPlot();
		plotMinError.setOutputName(outputName + "MinError");
		plotMinError.setTitle("OpCount Min Error");

		for (DistributionPlotSeries series : plotValues.getAllSeries()) {
			for (Entry<Long, DescriptiveStatistics> entry : series
					.getStatisticsMap().entrySet()) {

				double ratio = entry.getValue().getMin();

				plotMinValues.setValue(series.getName(), entry.getKey(), ratio);
				if (ratio < 1) {
					ratio = 1 / ratio;
				}
				plotMinError.setValue(series.getName(), entry.getKey(),
						100 * (ratio - 1));

			}
		}

		plotService.plot(plotValues);
		plotService.plot(plotMinValues);
		plotService.plot(plotMinError);
		plotService.plot(plotError);
	}
}