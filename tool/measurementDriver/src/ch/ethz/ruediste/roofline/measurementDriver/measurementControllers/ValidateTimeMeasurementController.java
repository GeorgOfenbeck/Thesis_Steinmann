package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.kernelAxis;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.exec.ExecuteException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Time;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;

import com.google.inject.Inject;

public class ValidateTimeMeasurementController extends
		ValidationMeasurementControllerBase implements IMeasurementController {

	public String getName() {
		return "valTime";
	}

	public String getDescription() {
		return "runs validation measurements of the execution time";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	@Inject
	MeasurementService measurementService;

	@Inject
	PlotService plotService;

	public void measure(String outputName) throws IOException {
		measureMem(outputName);
		measureArith(outputName);
	}

	public void measureArith(String outputName) throws ExecuteException,
			IOException {
		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();
		ParameterSpace space = new ParameterSpace();

		setupArithmeticKernels(space, kernelNames);

		// initialize plot
		DistributionPlot plotValues = new DistributionPlot();
		plotValues.setOutputName(outputName + "ArithValues")
				.setTitle("Time Values").setLog().setxLabel("expOperations")
				.setxUnit("operation").setyLabel("time").setyUnit("cycles");

		DistributionPlot plotMinValues = new DistributionPlot();
		plotMinValues.setOutputName(outputName + "ArithMinValues")
				.setTitle("Time Min Values").setLog().setxLabel("expOpCount")
				.setxUnit("1").setyLabel("min(time)").setyUnit("cycles");

		// iterate over space
		for (Coordinate coordinate : space) {
			// get the calculator for the transferred bytes
			QuantityCalculator<Time> calc = quantityMeasuringService
					.getExecutionTimeCalculator(ClockType.CoreCycles);

			// initialize the kernel
			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			// run the measurement
			QuantityMap result = quantityMeasuringService.measureQuantities(
					kernel, 100, calc);

			String kernelName = kernelNames.get(kernel);
			long expected = (long) kernel.getExpectedOperationCount()
					.getValue();

			fillDistributionPlots(kernelName, expected, plotValues,
					plotMinValues, result, calc);
		}

		DistributionPlot plotError = new DistributionPlot();
		plotError.setOutputName(outputName + "ArithError")
				.setTitle("Time Error").setLogX().setxLabel("expOpCount")
				.setKeyPosition(KeyPosition.TopLeft).setxUnit("operations")
				.setyLabel("err(time/min(time))").setyUnit("%")
				.setYRange(yErrorRange());

		fillErrorPlotMin(plotValues, plotError);

		DistributionPlot plotMinError = new DistributionPlot();
		plotMinError.setOutputName(outputName + "ArithMinError")
				.setTitle("Time Min Error").setLogX().setxLabel("expOpCount")
				.setKeyPosition(KeyPosition.TopRight).setxUnit("operations")
				.setyLabel("err(time10/min(time10))").setyUnit("%")
				.setYRange(yErrorRange());

		fillErrorPlotMin(plotMinValues, plotMinError);

		plotService.plot(plotValues);
		plotService.plot(plotMinValues);
		plotService.plot(plotError);
		plotService.plot(plotMinError);
	}

	public void measureMem(String outputName) throws ExecuteException,
			IOException {
		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();
		ParameterSpace space = new ParameterSpace();

		setupMemoryKernels(space, kernelNames);

		// initialize plot
		DistributionPlot plotValues = new DistributionPlot();
		plotValues.setOutputName(outputName + "MemValues")
				.setTitle("Time Values").setLog().setxLabel("expMemTransfer")
				.setxUnit("bytes").setyLabel("time").setyUnit("cycles");

		DistributionPlot plotMinValues = new DistributionPlot();
		plotMinValues.setOutputName(outputName + "MemMinValues")
				.setTitle("Time Min Values").setLog()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("time10").setyUnit("cycles");

		// iterate over space
		for (Coordinate coordinate : space) {
			// get the calculator for the transferred bytes
			QuantityCalculator<Time> calc = quantityMeasuringService
					.getExecutionTimeCalculator(ClockType.CoreCycles);

			// initialize the kernel
			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			QuantityMap result = quantityMeasuringService.measureQuantities(
					kernel, 100, calc);

			long expTransBytes = (long) kernel.getExpectedTransferredBytes()
					.getValue();
			String kernelName = kernelNames.get(kernel);

			fillDistributionPlots(kernelName, expTransBytes, plotValues,
					plotMinValues, result, calc);
		}

		DistributionPlot plotError = new DistributionPlot();
		plotError.setOutputName(outputName + "MemError");
		plotError.setTitle("Time Error").setLogX().setxLabel("expMemTransfer")
				.setxUnit("bytes").setyLabel("err(time/min(time))")
				.setyUnit("\\%").setYRange(yErrorRange())
				.setKeyPosition(KeyPosition.TopRight);

		DistributionPlot plotMinError = new DistributionPlot();
		plotMinError.setOutputName(outputName + "MemMinError")
				.setTitle("Time Min Error").setLogX()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("err(time10/min(time10))").setyUnit("\\%")
				.setYRange(yErrorRange()).setKeyPosition(KeyPosition.TopRight);

		fillErrorPlotMin(plotValues, plotError);

		fillErrorPlotMin(plotMinValues, plotMinError);

		plotService.plot(plotValues);
		plotService.plot(plotMinValues);
		plotService.plot(plotError);
		plotService.plot(plotMinError);
	}
}
