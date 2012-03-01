package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.single;
import static ch.ethz.ruediste.roofline.sharedEntities.Axes.kernelAxis;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.DistributionPlot.DistributionPlotSeries;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;

import com.google.inject.Inject;

public class ValidateTransferredBytesMeasurementController extends
		ValidationMeasurementControllerBase implements IMeasurementController {

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
		DistributionPlot plotTbValues = new DistributionPlot();
		plotTbValues.setOutputName(outputName + "ArithTBValues")
				.setTitle("Memory Transfer Values").setLog()
				.setxLabel("expOperationCount").setxUnit("flop")
				.setyLabel("actualMemTransfer").setyUnit("bytes");

		DistributionPlot plotTimeValues = new DistributionPlot();
		plotTimeValues.setOutputName(outputName + "ArithTimeValues")
				.setTitle("Memory Execution Time Values").setLog()
				.setxLabel("expOperationCount").setxUnit("flop")
				.setyLabel("actualMemTransfer").setyUnit("bytes");

		// iterate over space
		for (Coordinate coordinate : space) {
			// initialize the kernel
			final KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			// get the calculator for the transferred bytes
			QuantityCalculator<TransferredBytes> transferredBytesCalc = quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRam);

			QuantityCalculator<Time> executionTimeCalc = quantityMeasuringService
					.getExecutionTimeCalculator(ClockType.CoreCycles);

			IMeasurementBuilder builder = new IMeasurementBuilder() {

				public Measurement build(Map<String, MeasurerSet> sets) {
					Measurement measurement = new Measurement();
					Workload workload = new Workload();
					measurement.addWorkload(workload);
					workload.setKernel(kernel);
					workload.setMeasurerSet(sets.get("main"));
					return measurement;
				}
			};
			QuantityMap result = quantityMeasuringService
					.getQuantities(builder, 10)
					.with("main", transferredBytesCalc, executionTimeCalc)
					.get();

			long expOpCount = (long) kernel.getExpectedOperationCount()
					.getValue();

			// fill plots
			for (TransferredBytes transferredBytes : result
					.get(transferredBytesCalc)) {
				plotTbValues.addValue(kernelNames.get(kernel), expOpCount,
						transferredBytes.getValue());
			}
			for (Time time : result.get(executionTimeCalc)) {
				plotTimeValues.addValue(kernelNames.get(kernel), expOpCount,
						time.getValue());
			}
		}
		plotService.plot(plotTbValues);
		plotService.plot(plotTimeValues);
	}

	/**
	 * @param outputName
	 * @throws ExecuteException
	 * @throws IOException
	 */
	public void measureMem(String outputName) throws ExecuteException,
			IOException {
		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();
		ParameterSpace space = new ParameterSpace();

		setupMemoryKernels(space, kernelNames);

		// initialize plot
		DistributionPlot plotValues = new DistributionPlot();
		plotValues.setOutputName(outputName + "Values");
		plotValues.setTitle("Memory Values").setLog()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("actualMemTransfer/expMemTransfer").setyUnit("1");

		DistributionPlot plotError = new DistributionPlot();
		plotError.setOutputName(outputName + "Error");
		plotError.setTitle("Memory Error").setLog().setxLabel("expMemTransfer")
				.setxUnit("bytes")
				.setyLabel("err(actualMemTransfer/expMemTransfer)")
				.setyUnit("\\%");

		SeriesPlot plotMinValues = new SeriesPlot();
		plotMinValues.setOutputName(outputName + "MinValues");
		plotMinValues.setTitle("Memory Min Values").setLog()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("min(actualMemTransfer)/expMemTransfer")
				.setyUnit("1");

		SeriesPlot plotMinError = new SeriesPlot();
		plotMinError.setOutputName(outputName + "MinError");
		plotMinError.setTitle("Memory Min Error").setLog()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("err(min(actualMemTransfer)/expMemTransfer)")
				.setyUnit("\\%");

		// iterate over space
		for (Coordinate coordinate : space) {
			// initialize the kernel
			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			// get the calculator for the transferred bytes
			QuantityCalculator<TransferredBytes> calc = quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRam);
			MeasurerBase measurer = single(calc.getRequiredMeasurers());

			// setup the measurement
			Measurement measurement = new Measurement();
			Workload workload = new Workload();
			measurement.addWorkload(workload);
			workload.setKernel(kernel);
			workload.setMeasurerSet(new MeasurerSet(measurer));

			// run the measurement
			MeasurementResult result = measurementService.measure(measurement,
					10);

			// get the expected number of bytes transferred
			TransferredBytes expected = kernel.getExpectedTransferredBytes();

			// print results to console and fill plot
			/*System.out.printf("%s %s: expected: %s\n", kernelNames.get(kernel),
					coordinate, expected);*/
			for (MeasurementRunOutput runOutput : result.getRunOutputs()) {
				TransferredBytes actual = calc.getResult(Collections
						.singletonList(runOutput.getMeasurerOutput(measurer)));
				double ratio = actual.getValue() / expected.getValue();
				//System.out.printf("%s -> %g\n", actual, ratio);

				plotValues.addValue(kernelNames.get(kernel),
						(long) expected.getValue(),
						//coordinate.get(bufferSizeAxis),
						ratio);

				plotError.addValue(kernelNames.get(kernel),
						(long) expected.getValue(), toError(ratio));

			}
		}

		for (DistributionPlotSeries series : plotValues.getAllSeries()) {
			for (Entry<Long, DescriptiveStatistics> entry : series
					.getStatisticsMap().entrySet()) {

				double ratio = entry.getValue().getMin();

				System.out.printf("%s %d %e\n", series.getName(),
						entry.getKey(), ratio);
				plotMinValues.setValue(series.getName(), entry.getKey(), ratio);
				plotMinError.setValue(series.getName(), entry.getKey(),
						toError(ratio));
			}
		}

		plotService.plot(plotError);
		plotService.plot(plotValues);
		plotService.plot(plotMinError);
		plotService.plot(plotMinValues);
	}

}
