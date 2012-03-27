package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.bufferSizeAxis;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.DistributionNoExpectationController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Time;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.ArithmeticKernel.ArithmeticOperation;

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
	MeasurementService measurementService;

	@Inject
	PlotService plotService;

	public void measure(String outputName) throws IOException {

		instantiator.getInstance(ArithController.class).measure(
				outputName + "ThAdd",
				systemInfoService.getOnlineCPUs(),
				createArithKernelCoordinate(
						ArithmeticOperation.ArithmeticOperation_ADD,
						InstructionSet.SSE));

		measure(outputName, "Read", createReadKernelCoordinate());
		measure(outputName, "Write", createWriteKernelCoordinate());
		measure(outputName, "Triad", createTriadKernelCoordinate());

		instantiator.getInstance(ArithController.class).measure(
				outputName,
				cpuSingletonList(),
				createArithKernelCoordinates());

		instantiator.getInstance(MemController.class).measure(
				outputName,
				cpuSingletonList(),
				createMemKernelCoordinates());

	}

	/**
	 * @param outputName
	 * @param name
	 * @param kernelCoordinate
	 * @throws ExecuteException
	 * @throws IOException
	 */
	protected void measure(String outputName, String name,
			Coordinate kernelCoordinate)
			throws ExecuteException, IOException {
		MemController thRead = instantiator.getInstance(MemController.class);
		thRead.measure(
				outputName + "Th" + name,
				systemInfoService.getOnlineCPUs(),
				kernelCoordinate);

		MemController read = instantiator.getInstance(MemController.class);
		read.measure(
				outputName + name,
				cpuSingletonList(),
				kernelCoordinate);

		thRead.getPlotValues().addSeries(read.getPlotValues().getAllSeries());
		plotService.plot(thRead.getPlotValues());

		thRead.getPlotMinValues().addSeries(
				read.getPlotMinValues().getAllSeries());
		plotService.plot(thRead.getPlotMinValues());
	}

	static class ArithController extends
			DistributionNoExpectationController<Time> {

		@Override
		protected double getX(KernelBase kernel, long problemSize) {
			return kernel.getExpectedOperationCount().getValue();
		}

		@Override
		public void setupMinErrorPlot(String outputName,
				DistributionPlot plotMinError) {
			plotMinError.setOutputName(outputName + "ArithMinError")
					.setTitle("Time Min Error").setLogX()
					.setxLabel("expOpCount")
					.setKeyPosition(KeyPosition.TopRight)
					.setxUnit("operations")
					.setyLabel("err(time10/min(time10))").setyUnit("%");
		}

		@Override
		public void setupErrorPlot(String outputName, DistributionPlot plotError) {
			plotError.setOutputName(outputName + "ArithError")
					.setTitle("Time Error").setLogX().setxLabel("expOpCount")
					.setKeyPosition(KeyPosition.TopLeft).setxUnit("operations")
					.setyLabel("err(time/min(time))").setyUnit("%");
		}

		@Override
		public void setupMinValuesPlot(String outputName,
				DistributionPlot plotMinValues) {
			plotMinValues.setOutputName(outputName + "ArithMinValues")
					.setTitle("Time Min Values").setLog()
					.setxLabel("expOpCount")
					.setxUnit("1").setyLabel("min(time)").setyUnit("cycles");
		}

		@Override
		public void setupValuesPlot(String outputName,
				DistributionPlot plotValues) {
			plotValues.setOutputName(outputName + "ArithValues")
					.setTitle("Time Values").setLog()
					.setxLabel("expOperations")
					.setxUnit("operation").setyLabel("time").setyUnit("cycles");
		}

		@Override
		protected QuantityCalculator<Time> createCalculator(KernelBase kernel) {
			return quantityMeasuringService
					.getExecutionTimeCalculator(ClockType.CoreCycles);
		}

		@Override
		protected KernelBase createKernel(Coordinate kernelCoordinate,
				long problemSize) {
			ArithmeticKernel kernel = (ArithmeticKernel) KernelBase
					.create(kernelCoordinate);
			kernel.setIterations(problemSize);
			return kernel;
		}
	}

	static class MemController extends
			DistributionNoExpectationController<Time> {

		@Override
		protected KernelBase createKernel(Coordinate kernelCoordinate,
				long problemSize) {
			return KernelBase.create(kernelCoordinate.getExtendedPoint(
					bufferSizeAxis, problemSize));
		}

		@Override
		protected QuantityCalculator<Time> createCalculator(KernelBase kernel) {
			return quantityMeasuringService
					.getExecutionTimeCalculator(ClockType.CoreCycles);
		}

		@Override
		public void setupValuesPlot(String outputName,
				DistributionPlot plotValues) {
			plotValues.setOutputName(outputName + "MemValues")
					.setTitle("Time Values").setLog()
					.setxLabel("expMemTransfer")
					.setxUnit("bytes").setyLabel("time").setyUnit("cycles");
		}

		@Override
		public void setupMinValuesPlot(String outputName,
				DistributionPlot plotMinValues) {
			plotMinValues.setOutputName(outputName + "MemMinValues")
					.setTitle("Time Min Values").setLog()
					.setxLabel("expMemTransfer").setxUnit("bytes")
					.setyLabel("time10").setyUnit("cycles");
		}

		@Override
		public void setupErrorPlot(String outputName, DistributionPlot plotError) {
			plotError.setOutputName(outputName + "MemError")
					.setTitle("Time Error").setLogX()
					.setxLabel("expMemTransfer")
					.setxUnit("bytes").setyLabel("err(time/min(time))")
					.setKeyPosition(KeyPosition.TopRight);

		}

		@Override
		public void setupMinErrorPlot(String outputName,
				DistributionPlot plotMinError) {
			plotMinError.setOutputName(outputName + "MemMinError")
					.setTitle("Time Min Error").setLogX()
					.setxLabel("expMemTransfer").setxUnit("bytes")
					.setyLabel("err(time10/min(time10))").setyUnit("%")
					.setKeyPosition(KeyPosition.TopRight);
		}

		@Override
		protected double getX(KernelBase kernel, long problemSize) {
			return kernel.getExpectedTransferredBytes().getValue();
		}
	}
}
