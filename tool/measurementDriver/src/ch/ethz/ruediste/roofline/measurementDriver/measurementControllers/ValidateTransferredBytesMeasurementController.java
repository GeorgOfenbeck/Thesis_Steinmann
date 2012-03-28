package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.TransferredBytes;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.actions.*;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.*;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.WorkloadEventPredicate.WorkloadEventEnum;

import com.google.inject.Inject;

public class ValidateTransferredBytesMeasurementController extends
		ValidationMeasurementControllerBase implements IMeasurementController {

	private static final Logger log = Logger
			.getLogger(ValidateTransferredBytesMeasurementController.class);

	public String getName() {
		return "valTB";
	}

	public String getDescription() {
		return "runs validation measurements of the transferred bytes";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	@Inject
	MeasurementService measurementService;

	@Inject
	PlotService plotService;

	@Inject
	Configuration configuration;

	public void measure(String outputName) throws IOException {
		measureImp(outputName);

		log.info("Measuring ALT");
		configuration.push();
		configuration.set(QuantityMeasuringService.useAltTBKey, true);
		measureImp(outputName + "ALT");

		configuration.pop();
	}

	/**
	 * @param outputName
	 * @throws ExecuteException
	 * @throws IOException
	 */
	protected void measureImp(String outputName) throws ExecuteException,
			IOException {

		instantiator.getInstance(MemController.class).measure(
				outputName + "ThRead",
				systemInfoService.getOnlineCPUs(),
				createReadKernelCoordinate());

		instantiator.getInstance(MemController.class)
				.measure(outputName + "ThWrite",
						systemInfoService.getOnlineCPUs(),
						createWriteKernelCoordinate());

		instantiator.getInstance(MemController.class)
				.measure(outputName + "ThTriad",
						systemInfoService.getOnlineCPUs(),
						createTriadKernelCoordinate());

		instantiator.getInstance(ArithController.class).measure(outputName,
				cpuSingletonList(), createArithKernelCoordinates());

		instantiator.getInstance(MemController.class).measure(outputName,
				cpuSingletonList(), createMemKernelCoordinates());

		instantiator.getInstance(MemFlushController.class).measure(outputName,
				cpuSingletonList(), createMemKernelCoordinates());
	}

	private static class ArithController extends
			DistributionNoExpectationController<TransferredBytes> {

		@Override
		protected double getX(KernelBase kernel, long problemSize) {
			return kernel.getExpectedOperationCount().getValue();
		}

		@Override
		protected KernelBase createKernel(Coordinate kernelCoordinate,
				long problemSize) {
			return KernelBase.create(kernelCoordinate.getExtendedPoint(
					iterationsAxis, problemSize));
		}

		@Override
		protected QuantityCalculator<TransferredBytes> createCalculator(
				KernelBase kernel) {
			return quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRam);
		}

		@Override
		public void setupValuesPlot(String outputName,
				DistributionPlot plotValues) {
			plotValues.setOutputName(outputName + "ArithTBValues")
					.setTitle("Memory Transfer Values").setLog()
					.setxLabel("expOperationCount").setxUnit("flop")
					.setyLabel("actualMemTransfer").setyUnit("bytes")
					.setKeyPosition(KeyPosition.TopRight);

		}

		@Override
		public void setupMinValuesPlot(String outputName,
				DistributionPlot plotMinValues) {
			plotMinValues.setOutputName(outputName + "ArithTBMinValues")
					.setTitle("Memory Transfer Min Values").setLog()
					.setxLabel("expOperationCount").setxUnit("flop")
					.setyLabel("actualMemTransfer10").setyUnit("bytes")
					.setKeyPosition(KeyPosition.TopRight);
		}

		@Override
		public void setupErrorPlot(String outputName, DistributionPlot plotError) {
			plotError.setOutputName(outputName + "ArithTBError")
					.setTitle("Memory Transfer Error").setLogX()
					.setxLabel("expOperationCount").setxUnit("flop")
					.setyLabel("error(memTrans/min(memTrans))").setyUnit("%")
					.setKeyPosition(KeyPosition.TopRight);
		}

		@Override
		public void setupMinErrorPlot(String outputName,
				DistributionPlot plotMinError) {
			plotMinError.setOutputName(outputName + "ArithTBError")
					.setTitle("Memory Transfer Error").setLogX()
					.setxLabel("expOperationCount").setxUnit("flop")
					.setyLabel("error(memTrans10/min(memTrans10))")
					.setyUnit("%")
					.setKeyPosition(KeyPosition.TopRight);
		}

	}

	private static class MemFlushController extends
			DistributionNoExpectationController<TransferredBytes> {

		@Override
		protected double getX(KernelBase kernel, long problemSize) {
			return kernel.getExpectedTransferredBytes(
					systemInfoService.getSystemInformation()).getValue();
		}

		@Override
		protected boolean shouldContinue(double time, long problemSize,
				Coordinate kernelCoordinate) {
			double value = createKernel(kernelCoordinate, problemSize)
					.getExpectedTransferredBytes(
							systemInfoService.getSystemInformation())
					.getValue();
			return value < 1e8;
		}

		@Override
		protected KernelBase createKernel(Coordinate kernelCoordinate,
				long problemSize) {
			return KernelBase.create(kernelCoordinate.getExtendedPoint(
					bufferSizeAxis, problemSize));
		}

		@Override
		protected QuantityCalculator<TransferredBytes> createCalculator(
				KernelBase kernel) {
			return quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRam);
		}

		@Override
		protected Workload createWorkload(Measurement measurement,
				KernelBase kernel,
				MeasurerSet measurerSet) {
			Workload workload = new Workload();
			workload.setKernel(kernel);

			measurement
					.addRule(new Rule(
							new WorkloadEventPredicate(workload,
									WorkloadEventEnum.KernelStop),
							new MeasureActionExecutionAction(
									new FlushKernelBuffersAction(kernel),
									measurerSet)));

			return workload;
		}

		@Override
		public void setupValuesPlot(String outputName,
				DistributionPlot plotValues) {
			plotValues.setOutputName(outputName + "FlushValues")
					.setTitle("Transferred Bytes Flush Values").setLog()
					.setxLabel("expMemTransfer").setxUnit("bytes")
					.setyLabel("flushMemTransfer").setyUnit("bytes")
					.setKeyPosition(KeyPosition.TopLeft);
		}

		@Override
		public void setupMinValuesPlot(String outputName,
				DistributionPlot plotMinValues) {
			plotMinValues.setOutputName(outputName + "FlushMinValues")
					.setTitle("Transferred Bytes Flush Values").setLog()
					.setxLabel("expMemTransfer").setxUnit("bytes")
					.setyLabel("flushMemTransfer10").setyUnit("bytes")
					.setKeyPosition(KeyPosition.TopLeft);
		}

		@Override
		public void setupErrorPlot(String outputName, DistributionPlot plotError) {
			plotError.setOutputName(outputName + "FlushError")
					.setTitle("Transferred Bytes Flush Values").setLogX()
					.setxLabel("expMemTransfer").setxUnit("bytes")
					.setyLabel("err(flushMemTrans/min(flushMemTrans)")
					.setyUnit("%")
					.setKeyPosition(KeyPosition.TopLeft);
		}

		@Override
		public void setupMinErrorPlot(String outputName,
				DistributionPlot plotMinError) {
			plotMinError.setOutputName(outputName + "FlushMinError")
					.setTitle("Transferred Bytes Flush Values").setLogX()
					.setxLabel("expMemTransfer").setxUnit("bytes")
					.setyLabel("err(flushMemTrans10/min(flushMemTrans)")
					.setyUnit("%")
					.setKeyPosition(KeyPosition.TopLeft);
		}

	}

	private static class MemController extends
			DistributionWithExpectationController<TransferredBytes> {

		@Override
		protected TransferredBytes expected(KernelBase kernel) {
			return kernel.getExpectedTransferredBytes(systemInfoService
					.getSystemInformation());
		}

		@Override
		protected KernelBase createKernel(Coordinate kernelCoordinate,
				long problemSize) {
			return KernelBase.create(kernelCoordinate.getExtendedPoint(
					bufferSizeAxis, problemSize));
		}

		@Override
		protected QuantityCalculator<TransferredBytes> createCalculator(
				KernelBase kernel) {
			return quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRam);
		}

		@Override
		public void setupValuesPlot(String outputName,
				DistributionPlot plotValues) {
			plotValues.setOutputName(outputName + "Values")
					.setTitle("Transferred Bytes Values").setLog()
					.setxLabel("expMemTransfer").setxUnit("bytes")
					.setyLabel("actualMemTransfer/expMemTransfer")
					.setyUnit("1")
					.setKeyPosition(KeyPosition.TopRight);
		}

		@Override
		public void setupMinValuesPlot(String outputName,
				DistributionPlot plotMinValues) {
			plotMinValues.setOutputName(outputName + "MinValues")
					.setTitle("Transferred Bytes Min Values").setLog()
					.setxLabel("expMemTransfer").setxUnit("bytes")
					.setyLabel("actualMemTransfer10/expMemTransfer")
					.setyUnit("1");
		}

		@Override
		public void setupErrorPlot(String outputName, DistributionPlot plotError) {
			plotError.setOutputName(outputName + "Error")
					.setTitle("Transferred Bytes Error").setLogX()
					.setxLabel("expMemTransfer").setxUnit("bytes")
					.setyLabel("err(actualMemTransfer/expMemTransfer)")
					.setyUnit("%")
					.setKeyPosition(KeyPosition.TopRight);
		}

		@Override
		public void setupMinErrorPlot(String outputName,
				DistributionPlot plotMinError) {
			plotMinError.setOutputName(outputName + "MinError")
					.setTitle("Transferred Bytes Min Error").setLogX()
					.setxLabel("expMemTransfer").setxUnit("bytes")
					.setyLabel("err(actualMemTransfer10/expMemTransfer)")
					.setyUnit("%");
		}
	}
}
