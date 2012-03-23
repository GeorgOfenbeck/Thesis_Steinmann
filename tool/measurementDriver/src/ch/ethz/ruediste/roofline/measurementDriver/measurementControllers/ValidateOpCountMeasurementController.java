package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.kernelAxis;

import java.io.IOException;
import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.DistributionPlot;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
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

		setupIterations(space);

		// initialize plot
		DistributionPlot plotValues = new DistributionPlot();
		plotValues.setOutputName(outputName + "Values");
		plotValues.setTitle("OpCount Values").setLog().setxLabel("expOpCount")
				.setxUnit("flop").setyLabel("actualOpCount/expOpCount")
				.setyUnit("1");

		DistributionPlot plotMinValues = new DistributionPlot();
		plotMinValues.setOutputName(outputName + "MinValues");
		plotMinValues.setTitle("OpCount Min Values").setLog()
				.setxLabel("expOpCount").setxUnit("operations")
				.setyLabel("min(actualOpCount)/expOpCount").setyUnit("1");

		// iterate over space
		for (Coordinate coordinate : space) {
			// initialize the kernel
			final KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			// get the calculator for the op count
			QuantityCalculator<OperationCount> calc = quantityMeasuringService
					.getOperationCountCalculator(kernel.getSuggestedOperation());

			IMeasurementBuilder builder = new IMeasurementBuilder() {

				public Measurement build(Map<Object, MeasurerSet> sets) {

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
					.measureQuantities(builder, 100).with("operations", calc)
					.get();

			OperationCount expected = kernel.getExpectedOperationCount();

			fillDistributionPlotsExpected(kernelNames.get(kernel),
					expected.getValue(), plotValues, plotMinValues, quantities,
					calc);
		}

		DistributionPlot plotError = new DistributionPlot();
		plotError.setOutputName(outputName + "Error");
		plotError.setTitle("OpCount Error").setLog().setxLabel("expOpCount")
				.setxUnit("operations")
				.setyLabel("err(actualOpCount/expOpCount)").setyUnit("%");

		DistributionPlot plotMinError = new DistributionPlot();
		plotMinError.setOutputName(outputName + "MinError");
		plotMinError.setTitle("OpCount Min Error").setLog()
				.setxLabel("expOpCount").setxUnit("operations")
				.setyLabel("err(min(actualOpCount)/expOpCount)").setyUnit("%");

		fillErrorPlotExpected(plotValues, plotError);
		fillErrorPlotExpected(plotMinValues, plotMinError);

		plotService.plot(plotValues);
		plotService.plot(plotMinValues);
		plotService.plot(plotMinError);
		plotService.plot(plotError);
	}
}
