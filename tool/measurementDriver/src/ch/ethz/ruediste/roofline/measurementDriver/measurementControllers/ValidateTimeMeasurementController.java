package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.entities.Axes.*;
import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.single;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.entities.MeasurementResult;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.DistributionPlot.DistributionPlotSeries;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Time;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;

import com.google.inject.Inject;

public class ValidateTimeMeasurementController extends ValidationMeasurementControllerBase implements
		IMeasurementController {

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
		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();
		ParameterSpace space = new ParameterSpace();

		setupMemoryKernels(space, kernelNames);

		// initialize plot
		DistributionPlot plotValues = new DistributionPlot();
		plotValues.setOutputName(outputName + "Values");
		plotValues.setTitle("Time Values");

		SeriesPlot plotMinValues = new SeriesPlot();
		plotMinValues.setOutputName(outputName + "MinValues");
		plotMinValues.setTitle("Time Min Values");

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
			for (MeasurementRunOutput runOutput : result.getOutputs()) {
				Time actual = calc.getResult(Collections
						.singletonList(runOutput.getMeasurerOutput(measurer)));

				plotValues.addValue(kernelNames.get(kernel),
						coordinate.get(bufferSizeAxis), actual.getValue());

			}
		}

		for (DistributionPlotSeries series : plotValues.getAllSeries()) {
			for (Entry<Long, DescriptiveStatistics> entry : series
					.getStatisticsMap().entrySet()) {

				plotMinValues.setValue(series.getName(), entry.getKey(), entry
						.getValue().getMin());

			}
		}

		plotService.plot(plotValues);
		plotService.plot(plotMinValues);
	}

}
