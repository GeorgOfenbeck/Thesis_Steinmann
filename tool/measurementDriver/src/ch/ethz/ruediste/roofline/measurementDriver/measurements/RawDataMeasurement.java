package ch.ethz.ruediste.roofline.measurementDriver.measurements;

import java.io.IOException;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.ArithmeticKernelDescription;
import ch.ethz.ruediste.roofline.dom.ExecutionTimeMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.ExecutionTimeMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.KBestMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.dom.SimplePlot;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementCacheService;
import ch.ethz.ruediste.roofline.measurementDriver.services.PlotService;

import com.google.inject.Inject;

public class RawDataMeasurement implements IMeasurement {

	public String getName() {
		return "raw";
	}

	public String getDescription() {
		return "";
	}

	@Inject
	public MeasurementAppController measurementAppController;

	@Inject
	public MeasurementCacheService measurementCacheService;

	@Inject
	public PlotService plotService;

	public void measure(String outputName) throws IOException {
		// create schemes
		KBestMeasurementSchemeDescription kBestScheme = new KBestMeasurementSchemeDescription();
		SimpleMeasurementSchemeDescription simpleScheme = new SimpleMeasurementSchemeDescription();
		kBestScheme.setWarmCaches(false);
		simpleScheme.setWarmCaches(false);

		// create kernel
		// MemoryLoadKernelDescription kernel = new
		// MemoryLoadKernelDescription();
		// kernel.setBufferSize(2048);

		ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
		kernel.setIterations(100000);
		kernel.setUnroll(2);

		// create measurers
		PerfEventMeasurerDescription perfEventMeasurer = new PerfEventMeasurerDescription();
		perfEventMeasurer.addEvent("cycles", "perf::PERF_COUNT_HW_CPU_CYCLES");
		ExecutionTimeMeasurerDescription timeMeasurer = new ExecutionTimeMeasurerDescription();

		// measurement
		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setOptimization("-O3 -msse -msse2 -msse3");
		measurement.addMacro(ArithmeticKernelDescription.operationMacroName,
				"ArithmeticOperation_ADD");

		measurement.setKernel(kernel);
		measurement.setScheme(simpleScheme);
		measurement.setMeasurer(perfEventMeasurer);

		// perform measurement
		MeasurementResult result = measurementAppController.measure(
				measurement, 20);

		// create plot
		SimplePlot plot = new SimplePlot();
		if (measurement.getMeasurer() instanceof PerfEventMeasurerDescription) {
			PerfEventMeasurerOutput.addValues("cycles", result, plot);
		}

		if (measurement.getMeasurer() instanceof ExecutionTimeMeasurerDescription) {
			ExecutionTimeMeasurerOutput.addValues(result, plot);
		}

		plot.setTitle("%d:%s",
				// kernel.getBufferSize(),
				kernel.getIterations(),
				measurement.toString());
		plot.setOutputName("%s:%d:%s:%s", outputName,
				// kernel.getBufferSize(),
				kernel.getIterations(),
				measurement.toString(),
				measurement.getScheme().getWarmCaches() ? "warm" : "cold");

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
		System.out.println(summary.getStandardDeviation()
				/ summary.getMean());
		System.out.print("median:");
		System.out.println(summary.getPercentile(50));
		System.out.print("min:");
		System.out.println(summary.getMin());
		System.out.print("max:");
		System.out.println(summary.getMax());
		System.out.println();
	}

}
