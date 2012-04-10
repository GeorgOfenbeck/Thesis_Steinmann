package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.bufferSizeAxis;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Time;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
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

		measure(outputName, "Add", createArithKernelCoordinate(
				ArithmeticOperation.ArithmeticOperation_ADD,
				InstructionSet.SSE), ArithController.class);

		measure(outputName, "Read", createReadKernelCoordinate(),
				MemController.class);
		measure(outputName, "Write", createWriteKernelCoordinate(true, false),
				MemController.class);
		measure(outputName, "WriteStream",
				createWriteKernelCoordinate(true, true),
				MemController.class);
		measure(outputName, "Triad", createTriadKernelCoordinate(),
				MemController.class);

		instantiator.getInstance(ArithController.class).measure(
				outputName,
				cpuSingletonList(),
				createArithKernelCoordinates());

		instantiator.getInstance(MemController.class).measure(
				outputName,
				cpuSingletonList(),
				createMemKernelCoordinates());

		//measureHistogram(outputName, createTriadKernelCoordinate());
	}

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public SystemInfoService systemInfoService;

	/*private void measure2(String outputName) {
		final List<Integer> cpus = systemInfoService.getOnlineCPUs();
		final Coordinate kernelCoordinate = createReadKernelCoordinate();
		long problemSize = 256;
		DistributionPlot plotValues = new DistributionPlot();

		plotValues.setOutputName(outputName + "Mem2Values")
				.setTitle("Time Values").setLog()
				.setxLabel("Expected Transfer Volume")
				.setxUnit("Bytes").setyLabel("time").setyUnit("Cycles");

		ArrayList<QuantityCalculator<Time>> calcs = new ArrayList<QuantityCalculator<Time>>();
		for (int cpu : cpus) {
			calcs.add(quantityMeasuringService
					.getExecutionTimeCalculator(ClockType.CoreCycles));
		}

		QuantityCalculator<Time> timeCalc = quantityMeasuringService
				.getExecutionTimeCalculator(ClockType.uSecs);
		double time = 0;

		while (time < 1e5) {
			IMeasurementBuilder builder = new IMeasurementBuilder() {

				public Measurement build(Map<Object, MeasurerSet> sets) {
					Measurement measurement = new Measurement();
					for (int cpu : cpus) {
						Workload workload = new Workload();
						measurement.addWorkload(workload);

						KernelBase kernel = KernelBase
								.create(kernelCoordinate.getExtendedPoint(
										bufferSizeAxis, problemSize));
						workload.setKernel(kernel);
					}
					return measurement;
				}
			};

			QuantityMap result = quantityMeasuringService
					.measureQuantities(builder, 10).with("main", calc).get();
		}
		plotService.plot(plotValues);
	}*/

	private void measureHistogram(String outputName,
			Coordinate... kernelCoordinates) throws ExecuteException,
			IOException {
		HistogramPlot plot = new HistogramPlot();
		plot.setOutputName(outputName + "Hist").setTitle("Hist")
				.setBinCount(40).setXRange(0.8, 1.2);

		for (Coordinate kernelCoordinate : kernelCoordinates) {
			double time = 0;
			long problemSizes[] = new long[] { 1024L * 16, 1024L * 128,
					1024 * 256L };

			//while (time < 1e2) {
			for (long problemSize : problemSizes) {
				KernelBase kernel = KernelBase.create(
						kernelCoordinate.getExtendedPoint(bufferSizeAxis,
								problemSize));

				QuantityCalculator<Time> calcCycle = quantityMeasuringService
						.getExecutionTimeCalculator(ClockType.CoreCycles);
				QuantityCalculator<Time> calcUSecs = quantityMeasuringService
						.getExecutionTimeCalculator(ClockType.uSecs);

				QuantityMap result = quantityMeasuringService
						.measureQuantities(kernel, 200, calcCycle, calcUSecs);

				DescriptiveStatistics stats = result.getStatistics(calcCycle);
				String label = String.format("%s%.0f", kernel.getLabel(),
						+kernel.getExpectedTransferredBytes(
								systemInfoService
										.getSystemInformation())
								.getValue());

				DescriptiveStatistics stat = new DescriptiveStatistics();
				for (Time cycles : result.get(calcCycle)) {
					/*stat.addValue(cycles.getValue() / stats.getMean());
					if (stat.getN() >= 10) {
						plot.addValue(label, stat.getMin());
					}*/
					plot.addValue(label,
							cycles.getValue() / stats.getMean());

				}

				time = result.min(calcUSecs).getValue();
				problemSize *= 2;
			}
		}

		plotService.plot(plot);
	}

	/**
	 * @param outputName
	 * @param name
	 * @param kernelCoordinate
	 * @param clazz
	 *            TODO
	 * @throws ExecuteException
	 * @throws IOException
	 */
	protected void measure(String outputName, String name,
			Coordinate kernelCoordinate,
			Class<? extends DistributionControllerBase<?>> clazz)
			throws ExecuteException, IOException {
		DistributionControllerBase<?> thRead = instantiator.getInstance(clazz);
		thRead.measure(
				outputName + "Th" + name,
				systemInfoService.getOnlineCPUs(),
				kernelCoordinate);

		DistributionControllerBase<?> read = instantiator.getInstance(clazz);
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
					.setKeyPosition(KeyPosition.TopRight)
					.setxUnit("operations")
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
		protected double getReference(DescriptiveStatistics statistcs) {
			return statistcs.getMin();
		}

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
					.setxLabel("Expected Transfer Volume")
					.setxUnit("Bytes").setyLabel("time").setyUnit("Cycles");
		}

		@Override
		public void setupMinValuesPlot(String outputName,
				DistributionPlot plotMinValues) {
			plotMinValues.setOutputName(outputName + "MemMinValues")
					.setTitle("Time Min Values").setLog()
					.setxLabel("Expected Transfer Volume").setxUnit("Bytes")
					.setyLabel("time10").setyUnit("Cycles");
		}

		@Override
		public void setupErrorPlot(String outputName, DistributionPlot plotError) {
			plotError.setOutputName(outputName + "MemError")
					.setTitle("Time Error").setLogX()
					.setxLabel("Expected Transfer Volume")
					.setxUnit("Bytes").setyLabel("err(time/min(time))")
					.setKeyPosition(KeyPosition.TopRight);

		}

		@Override
		public void setupMinErrorPlot(String outputName,
				DistributionPlot plotMinError) {
			plotMinError.setOutputName(outputName + "MemMinError")
					.setTitle("Time Min Error").setLogX()
					.setxLabel("Expected Transfer Volume").setxUnit("Bytes")
					.setyLabel("err(time10/min(time10))").setyUnit("%")
					.setKeyPosition(KeyPosition.TopRight);
		}

		@Override
		protected double getX(KernelBase kernel, long problemSize) {
			SystemInformation systemInformation = systemInfoService
					.getSystemInformation();
			return kernel.getExpectedTransferredBytes(systemInformation)
					.getValue();
		}
	}
}
