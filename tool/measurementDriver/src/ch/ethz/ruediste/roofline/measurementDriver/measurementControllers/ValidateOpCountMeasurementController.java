package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.iterationsAxis;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.DistributionWithExpectationController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.DistributionPlot;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.ArithmeticKernel.ArithmeticOperation;

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
		instantiator.getInstance(OpCountController.class).measure(
				outputName + "Th",
				systemInfoService.getOnlineCPUs(),
				createArithKernelCoordinate(
						ArithmeticOperation.ArithmeticOperation_ADD,
						InstructionSet.x87));

		instantiator.getInstance(OpCountController.class).measure(
				outputName,
				cpuSingletonList(),
				createArithKernelCoordinates());
	}

	private static class OpCountController extends
			DistributionWithExpectationController<OperationCount> {

		@Override
		protected KernelBase createKernel(Coordinate kernelCoordinate,
				long problemSize) {
			return KernelBase.create(kernelCoordinate
					.getExtendedPoint(
							iterationsAxis, problemSize));
		}

		@Override
		protected QuantityCalculator<OperationCount> createCalculator(
				KernelBase kernel) {
			return quantityMeasuringService
					.getOperationCountCalculator(kernel.getSuggestedOperation());
		}

		@Override
		public void setupValuesPlot(String outputName,
				DistributionPlot plotValues) {
			plotValues.setOutputName(outputName + "Values")
					.setTitle("Operation Count Values").setLog()
					.setxLabel("Expected Operation Count")
					.setxUnit("Flops").setyLabel("Operation Count Ratio")
					.setyUnit("1");
		}

		@Override
		public void setupMinValuesPlot(String outputName,
				DistributionPlot plotMinValues) {
			plotMinValues.setOutputName(outputName + "MinValues")
					.setTitle("Operation Count Min Values").setLog()
					.setxLabel("Expected Operation Count")
					.setxUnit("Flops")
					.setyLabel("Operation Count Ratio").setyUnit("1");
		}

		@Override
		public void setupErrorPlot(String outputName, DistributionPlot plotError) {
			plotError.setOutputName(outputName + "Error")
					.setTitle("OpCount Error").setLogX()
					.setxLabel("expOpCount")
					.setxUnit("Flops")
					.setyLabel("err(actualOpCount/expOpCount)").setyUnit("%");

		}

		@Override
		public void setupMinErrorPlot(String outputName,
				DistributionPlot plotMinError) {
			plotMinError.setOutputName(outputName + "MinError")
					.setTitle("Operation Count Min Error").setLogX()
					.setxLabel("Expected Operation Count")
					.setxUnit("Flops")
					.setyLabel("Error")
					.setyUnit("%");
		}

		@Override
		protected OperationCount expected(KernelBase kernel) {
			return kernel
					.getExpectedOperationCount();
		}

	}
}
