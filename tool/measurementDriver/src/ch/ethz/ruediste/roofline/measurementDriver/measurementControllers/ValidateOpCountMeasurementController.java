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
import ch.ethz.ruediste.roofline.sharedEntities.KernelBase;

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
				outputName,
				cpuSingletonList(),
				createReadKernelCoordinate(),
				createWriteKernelCoordinate(),
				createTriadKernelCoordinate());
	}

	private static class OpCountController extends
			DistributionWithExpectationController<OperationCount> {

		@Override
		protected KernelBase createKernel(Coordinate kernelCoordinate,
				long problemSize) {
			return KernelBase.create(kernelCoordinate.getExtendedPoint(
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
					.setTitle("OpCount Values").setLog()
					.setxLabel("expOpCount")
					.setxUnit("flop").setyLabel("actualOpCount/expOpCount")
					.setyUnit("1");
		}

		@Override
		public void setupMinValuesPlot(String outputName,
				DistributionPlot plotMinValues) {
			plotMinValues.setOutputName(outputName + "MinValues")
					.setTitle("OpCount Min Values").setLog()
					.setxLabel("expOpCount").setxUnit("operations")
					.setyLabel("min(actualOpCount)/expOpCount").setyUnit("1");
		}

		@Override
		public void setupErrorPlot(String outputName, DistributionPlot plotError) {
			plotError.setOutputName(outputName + "Error")
					.setTitle("OpCount Error").setLog().setxLabel("expOpCount")
					.setxUnit("operations")
					.setyLabel("err(actualOpCount/expOpCount)").setyUnit("%");

		}

		@Override
		public void setupMinErrorPlot(String outputName,
				DistributionPlot plotMinError) {
			plotMinError.setOutputName(outputName + "MinError")
					.setTitle("OpCount Min Error").setLog()
					.setxLabel("expOpCount").setxUnit("operations")
					.setyLabel("err(min(actualOpCount)/expOpCount)")
					.setyUnit("%");
		}

		@Override
		protected OperationCount expected(KernelBase kernel) {
			return kernel
					.getExpectedOperationCount();
		}

	}
}
