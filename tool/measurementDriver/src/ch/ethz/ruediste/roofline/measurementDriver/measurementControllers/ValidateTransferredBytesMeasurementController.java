package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;
import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;
import java.util.*;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
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
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.RunQuantityMap;
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

		measureThreadedMem(outputName, "ThRead", createReadKernelCoordinate());
		measureThreadedMem(outputName, "ThWrite", createWriteKernelCoordinate());
		measureThreadedMem(outputName, "ThTriad", createTriadKernelCoordinate());

		instantiator.getInstance(MemController.class).measure(outputName,
				cpuSingletonList(), createMemKernelCoordinates());

		instantiator.getInstance(ArithController.class).measure(outputName,
				cpuSingletonList(), createArithKernelCoordinates());

		instantiator.getInstance(MemFlushController.class).measure(outputName,
				cpuSingletonList(), createMemKernelCoordinates());
	}

	/**
	 * @param outputName
	 * @param name
	 * @param kernelCoordinate
	 * @throws ExecuteException
	 * @throws IOException
	 */
	protected void measureThreadedMem(String outputName, String name,
			Coordinate kernelCoordinate) throws ExecuteException, IOException {
		MemController ctrl = instantiator.getInstance(MemController.class);
		ctrl.measure(
				outputName + name,
				toList(head(systemInfoService.getOnlineCPUs())),
				kernelCoordinate);

		MemController thCtrl = instantiator.getInstance(MemController.class);
		thCtrl.measure(
				outputName + name,
				systemInfoService.getOnlineCPUs(),
				kernelCoordinate);

		thCtrl.getPlotValues().addSeries(ctrl.getPlotValues().getAllSeries());
		thCtrl.getPlotMinValues().addSeries(
				ctrl.getPlotMinValues().getAllSeries());
		plotService.plot(thCtrl.getPlotValues());
		plotService.plot(thCtrl.getPlotMinValues());

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
					.setKeyPosition(KeyPosition.TopLeft);

		}

		@Override
		public void setupMinValuesPlot(String outputName,
				DistributionPlot plotMinValues) {
			plotMinValues.setOutputName(outputName + "ArithTBMinValues")
					.setTitle("Memory Transfer Min Values").setLog()
					.setxLabel("expOperationCount").setxUnit("flop")
					.setyLabel("actualMemTransfer10").setyUnit("bytes")
					.setKeyPosition(KeyPosition.TopLeft);
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

		long sz = 1024 * 256;
		PointPlot plotPoint = new PointPlot();
		SimplePlot plotSimple = new SimplePlot();

		@Override
		protected void measureEnter(String outputName, List<Integer> cpus,
				Coordinate[] kernelCoordinates) {
			plotPoint.setOutputName(outputName + "Point")
					.setTitle("Result Distribution")
					.setxLabel("Transfer Volume Core 0").setxUnit("Bytes")
					.setyLabel("Transfer Volume Core 1").setyUnit("Bytes");
			plotSimple.setOutputName(outputName + "Raw")
					.setTitle("Raw Results")
					.setxLabel("Measurement Run Number").setxUnit("1")
					.setyLabel("Transfer Volume").setyUnit("Bytes");
		}

		@Override
		protected void additionalResultProcessing(long problemSize,
				QuantityMap result,
				ArrayList<KernelBase> kernels,
				ArrayList<QuantityCalculator<TransferredBytes>> calcs,
				List<Integer> cpus) {
			if (cpus.size() != 2)
				return;
			if (problemSize == sz) {
				String name = getHistogramName(kernels.get(0), problemSize);

				double last = Double.NaN;
				double th;
				{
					DescriptiveStatistics stat = result.getStatistics(calcs
							.get(0));
					th = (stat.getMax() + stat.getMin()) / 2;
				}

				int hh = 0;
				int hl = 0;
				int ll = 0;
				int lh = 0;
				for (RunQuantityMap runMap : result.getRunMaps()) {
					plotPoint.addValue(name, runMap.get(calcs.get(0))
							.getValue(), runMap.get(calcs.get(1)).getValue());
					plotSimple.apply(runMap.get(calcs.get(0)).getValue());

					double cur = runMap.get(calcs.get(0)).getValue();
					if (last != Double.NaN) {
						if (last > th && cur > th)
							hh++;
						if (last > th && cur <= th)
							hl++;
						if (last <= th && cur > th)
							lh++;
						if (last <= th && cur <= th)
							ll++;
					}
					last = cur;
				}
				double h = hh + hl;
				double l = ll + lh;
				System.out.printf("\t0\t1\n");
				System.out.printf("0\t%e\t%e\n", ll / l, lh / l);
				System.out.printf("1\t%e\t%e\n", hl / h, hh / h);
				System.out.printf("x\t%e\t%e\n", l / (h + l), h / (h + l));
			}
		}

		@Override
		protected void measureLeave() throws ExecuteException, IOException {
			plotService.plot(plotPoint);
			plotService.plot(plotSimple);
		}

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
					.setTitle("Transfer Volume").setLog()
					.setxLabel("Expected Memory Transfer").setxUnit("Bytes")
					.setyLabel("actMemTransfer/expMemTransfer")
					.setyUnit("1")
					.setYRange(0.8, 10)
					.setKeyPosition(KeyPosition.TopRight);
		}

		@Override
		public void setupMinValuesPlot(String outputName,
				DistributionPlot plotMinValues) {
			plotMinValues.setOutputName(outputName + "MinValues")
					.setTitle("Transfer Volume Min Values").setLog()
					.setxLabel("Expected Memory Transfer").setxUnit("Bytes")
					.setyLabel("actualMemTransfer10/expMemTransfer")
					.setyUnit("1");
		}

		@Override
		public void setupErrorPlot(String outputName, DistributionPlot plotError) {
			plotError.setOutputName(outputName + "Error")
					.setTitle("Transfer Volume Error").setLogX()
					.setxLabel("Expected Memory Transfer").setxUnit("Bytes")
					.setyLabel("err(actualMemTransfer/expMemTransfer)")
					.setyUnit("%")
					.setKeyPosition(KeyPosition.TopRight);
		}

		@Override
		public void setupMinErrorPlot(String outputName,
				DistributionPlot plotMinError) {
			plotMinError.setOutputName(outputName + "MinError")
					.setTitle("Transfer Volume Min Error").setLogX()
					.setxLabel("Expected Memory Transfer").setxUnit("Bytes")
					.setyLabel("err(actualMemTransfer10/expMemTransfer)")
					.setKeyPosition(KeyPosition.TopRight)
					.setyUnit("%");
		}

		@Override
		public void setupHistogramPlot(String outputName, HistogramPlot plotHist) {
			plotHist.setOutputName(outputName + "Hist")
					.setTitle(
							"Transfer Volume Histogram")
					//.setXRange(0.9, 2.5)
					.setBinCount(20).setxLabel("Transfer Volume")
					.setxUnit("Bytes").setyLabel("Number of Results")
					.setyUnit("1");
		}

		@Override
		public String getHistogramName(KernelBase kernel, long problemSize) {
			if (problemSize == sz) {
				return String.format("%s %.0f",
						kernel.getLabel(),
						kernel.getExpectedTransferredBytes(systemInfoService
								.getSystemInformation()).getValue());
			}
			return null;
		}
	}
}
