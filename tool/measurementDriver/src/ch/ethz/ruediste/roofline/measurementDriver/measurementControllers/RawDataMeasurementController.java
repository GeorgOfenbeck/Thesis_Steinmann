package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.ArithmeticKernelDescription.ArithmeticOperation;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.SimplePlot;
import ch.ethz.ruediste.roofline.measurementDriver.services.PlotService;

import com.google.inject.Inject;

public class RawDataMeasurementController implements IMeasurementController {

	public String getName() {
		return "raw";
	}

	public String getDescription() {
		return "";
	}

	@Inject
	MeasurementAppController measurementAppController;

	@Inject
	public PlotService plotService;

	public void measure(String outputName) throws IOException {
		// create schemes
		SimpleMeasurementSchemeDescription simpleScheme = new SimpleMeasurementSchemeDescription();
		simpleScheme.setWarmCaches(false);

		// create kernel
		// MemoryLoadKernelDescription kernel = new
		// MemoryLoadKernelDescription();
		// kernel.setBufferSize(2048);

		ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
		kernel.setIterations(100000);
		kernel.setUnroll(2);
		kernel.setOperation(ArithmeticOperation.ArithmeticOperation_ADD);
		kernel.setOptimization("-O3 -msse -msse2 -msse3");

		// create measurers
		PerfEventMeasurerDescription perfEventMeasurer = new PerfEventMeasurerDescription();
		perfEventMeasurer.addEvent("cycles", "perf::PERF_COUNT_HW_CPU_CYCLES");
		ExecutionTimeMeasurerDescription timeMeasurer = new ExecutionTimeMeasurerDescription();

		// measurement
		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(kernel);
		measurement.setScheme(simpleScheme);
		measurement.setMeasurer(perfEventMeasurer);

		// perform measurement
		MeasurementResult result = measurementAppController.measure(
				measurement, 20);

		// create plot
		SimplePlot plot = new SimplePlot();
		if (measurement.getMeasurer() instanceof PerfEventMeasurerDescription) {
			perfEventMeasurer.addValues("cycles", result, plot);
		}

		if (measurement.getMeasurer() instanceof ExecutionTimeMeasurerDescription) {
			timeMeasurer.addValues(result, plot);
		}

		plot.setTitle("%d:%s",
				// kernel.getBufferSize(),
				kernel.getIterations(), measurement.toString());
		plot.setOutputName("%s:%d:%s:%s", outputName,
				// kernel.getBufferSize(),
				kernel.getIterations(), measurement.toString(), measurement
						.getScheme().getWarmCaches() ? "warm" : "cold");

		plotService.plot(plot);
	}

	static void printSummary(DescriptiveStatistics summary) {
		System.out.println("Measurement");
		System.out.print("number of outputs: ");
		System.out.println(summary.getN());
		System.out.print("mean:");
		System.out.println(summary.getMean());
		System.out.print("stddev:");
		System.out.println(summary.getStandardDeviation());
		System.out.print("relative:");
		System.out.println(summary.getStandardDeviation() / summary.getMean());
		System.out.print("median:");
		System.out.println(summary.getPercentile(50));
		System.out.print("min:");
		System.out.println(summary.getMin());
		System.out.print("max:");
		System.out.println(summary.getMax());
		System.out.println();
	}

}
