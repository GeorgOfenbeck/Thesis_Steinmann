package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.single;
import static ch.ethz.ruediste.roofline.sharedEntities.Axes.kernelAxis;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.DistributionPlot.DistributionPlotSeries;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Time;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
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

		// iterate over space
		for (Coordinate coordinate : space) {
			// get the calculator for the transferred bytes
			QuantityCalculator<Time> calc = quantityMeasuringService
					.getExecutionTimeCalculator(ClockType.CoreCycles);

			// initialize the kernel
			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			// get the meaurer
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

			// print results to console and fill plot
			/*System.out.printf("%s %s: expected: %s\n", kernelNames.get(kernel),
					coordinate, expected);*/
			for (MeasurementRunOutput runOutput : result.getRunOutputs()) {
				Time actual = calc.getResult(Collections
						.singletonList(runOutput
								.getMeasurerOutputUntyped(measurer)));

				plotValues.addValue(kernelNames.get(kernel), (long) kernel
						.getExpectedOperationCount().getValue(), actual
						.getValue());

			}
		}

		DistributionPlot plotError = new DistributionPlot();
		plotError.setOutputName(outputName + "ArithError")
				.setTitle("Time Error").setLogX().setxLabel("expOpCount")
				.setxUnit("opreations").setyLabel("err(cycles/median(cycles))")
				.setyUnit("%");

		SeriesPlot plotMinValues = new SeriesPlot();
		plotMinValues.setOutputName(outputName + "ArithMinValues")
				.setTitle("Time Min Values").setLog().setxLabel("expOpCount")
				.setxUnit("1").setyLabel("min(cycles)").setyUnit("1");

		for (DistributionPlotSeries series : plotValues.getAllSeries()) {
			for (Entry<Long, DescriptiveStatistics> entry : series
					.getStatisticsMap().entrySet()) {

				plotMinValues.setValue(series.getName(), entry.getKey(), entry
						.getValue().getMin());

				double median = entry.getValue().getPercentile(50);

				for (double value : entry.getValue().getValues()) {
					plotError.addValue(series.getName(), entry.getKey(),
							toError(value / median));
				}
			}
		}

		plotService.plot(plotValues);
		plotService.plot(plotMinValues);
		plotService.plot(plotError);
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

		// iterate over space
		for (Coordinate coordinate : space) {
			// get the calculator for the transferred bytes
			QuantityCalculator<Time> calc = quantityMeasuringService
					.getExecutionTimeCalculator(ClockType.CoreCycles);

			// initialize the kernel
			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			// get the meaurer
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

			// print results to console and fill plot
			/*System.out.printf("%s %s: expected: %s\n", kernelNames.get(kernel),
					coordinate, expected);*/
			for (MeasurementRunOutput runOutput : result.getRunOutputs()) {
				Time actual = calc.getResult(Collections
						.singletonList(runOutput
								.getMeasurerOutputUntyped(measurer)));

				plotValues.addValue(kernelNames.get(kernel), (long) kernel
						.getExpectedTransferredBytes().getValue(), actual
						.getValue());

			}
		}

		DistributionPlot plotError = new DistributionPlot();
		plotError.setOutputName(outputName + "MemError");
		plotError.setTitle("Time Error").setLogX().setxLabel("expMemTransfer")
				.setxUnit("bytes").setyLabel("err(time/median(time))")
				.setyUnit("\\%");

		SeriesPlot plotMinValues = new SeriesPlot();
		plotMinValues.setOutputName(outputName + "MemMinValues");
		plotMinValues.setTitle("Time Min Values").setLog()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("min(time)").setyUnit("cycles");

		for (DistributionPlotSeries series : plotValues.getAllSeries()) {
			for (Entry<Long, DescriptiveStatistics> entry : series
					.getStatisticsMap().entrySet()) {

				plotMinValues.setValue(series.getName(), entry.getKey(), entry
						.getValue().getMin());

				double median = entry.getValue().getPercentile(50);

				for (double value : entry.getValue().getValues()) {
					plotError.addValue(series.getName(), entry.getKey(),
							toError(value / median));
				}
			}
		}

		plotService.plot(plotValues);
		plotService.plot(plotMinValues);
		plotService.plot(plotError);
	}
}
