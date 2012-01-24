package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.HistogramPlot;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;

import com.google.inject.Inject;

public class VarianceHistogramMeasurementController implements
		IMeasurementController {

	public String getName() {
		return "varianceHist";
	}

	public String getDescription() {
		return "";
	}

	@Inject
	MeasurementAppController measurementAppController;

	@Inject
	public CommandService commandService;

	@Inject
	public PlotService plotService;

	public void measure(String outputName) throws IOException {
		// create schemes
		SimpleMeasurementSchemeDescription simpleScheme = new SimpleMeasurementSchemeDescription();
		simpleScheme.setWarmCaches(false);

		// create kernel
		MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
		kernel.setOptimization("-O0");
		kernel.setBufferSize(2048);

		// create measurers
		PerfEventMeasurerDescription perfEventMeasurer = new PerfEventMeasurerDescription();
		perfEventMeasurer.addEvent("cycles", "perf::PERF_COUNT_HW_BUS_CYCLES");
		ExecutionTimeMeasurerDescription timeMeasurer = new ExecutionTimeMeasurerDescription();

		// measurement
		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(kernel);
		measurement.setScheme(simpleScheme);
		measurement.setMeasurer(perfEventMeasurer);

		// perform measurement
		// measurementCacheService.deleteFromCache(measurement);
		MeasurementResult result = measurementAppController.measure(
				measurement, 100);

		// create statistics
		HistogramPlot plot = new HistogramPlot();
		if (measurement.getMeasurer() instanceof PerfEventMeasurerDescription) {
			perfEventMeasurer.addValues("cycles", result, plot);
		}

		if (measurement.getMeasurer() instanceof ExecutionTimeMeasurerDescription) {
			timeMeasurer.addValues(result, plot);
		}

		plot.setTitle("%d:%s", kernel.getBufferSize(), measurement.toString());
		plot.setOutputName("%s:%d:%s:%s", outputName, kernel.getBufferSize(),
				measurement.toString(),
				measurement.getScheme().getWarmCaches() ? "warm" : "cold");
		plotService.plot(plot);
	}
}
