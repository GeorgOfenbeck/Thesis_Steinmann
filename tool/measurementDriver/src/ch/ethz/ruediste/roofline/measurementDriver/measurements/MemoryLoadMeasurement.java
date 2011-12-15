package ch.ethz.ruediste.roofline.measurementDriver.measurements;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.ExecutionTimeMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.ExecutionTimeMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.MemoryLoadKernelDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.dom.HistogramPlot;
import ch.ethz.ruediste.roofline.measurementDriver.dom.SimplePlot;
import ch.ethz.ruediste.roofline.measurementDriver.services.PlotService;

import com.google.inject.Inject;

public class MemoryLoadMeasurement implements IMeasurement {

	public String getName() {
		return "memoryLoad";
	}

	public String getDescription() {
		return "loads a memory buffer and measures the memory throughput";
	}

	@Inject
	public MeasurementAppController measurementAppController;

	@Inject
	public PlotService plotService;

	public void measure(String outputName) throws IOException {
		MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
		kernel.setBufferSize(1024*1024*40);

		SimpleMeasurementSchemeDescription scheme = new SimpleMeasurementSchemeDescription();
		scheme.setWarmCaches(false);

		PerfEventMeasurerDescription measurer = new PerfEventMeasurerDescription();
		//measurer.addEvent("trans", "core::BUS_TRANS_BURST:SELF");
		measurer.addEvent("inst", "core::INSTRUCTION_RETIRED");
		//measurer.addEvent("cycles", "core::UNHALTED_CORE_CYCLES");
		
		//ExecutionTimeMeasurerDescription measurer=new ExecutionTimeMeasurerDescription();
		

		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(kernel);
		measurement.setScheme(scheme);
		measurement.setMeasurer(measurer);
		measurement.setOptimization("-O3");

		// perform measurement
		MeasurementResult result = measurementAppController.measure(
				measurement, 10);

		/*// simple plot
		{
			SimplePlot plot = new SimplePlot();

			ExecutionTimeMeasurerOutput.addValues(result, plot);

			plot.setTitle("Load, BufferSize: %d", kernel.getBufferSize());
			plot.setOutputName("%s:usec:%d:%s", outputName,
					kernel.getBufferSize(),
					measurement.getScheme().getWarmCaches() ? "warm" : "cold");
			plotService.plot(plot);
		}*/
				
		/*// simple plot
		{
			SimplePlot plot = new SimplePlot();

			PerfEventMeasurerOutput.addValues("trans", result, plot);

			plot.setTitle("Load, BufferSize: %d", kernel.getBufferSize());
			plot.setOutputName("%s:trans:%d:%s", outputName,
					kernel.getBufferSize(),
					measurement.getScheme().getWarmCaches() ? "warm" : "cold");
			plotService.plot(plot);
		}*/
		
	/*	// simple plot
		{
			SimplePlot plot = new SimplePlot();

			PerfEventMeasurerOutput.addValues("cycles", result, plot);

			plot.setTitle("Load, BufferSize: %d", kernel.getBufferSize());
			plot.setOutputName("%s:cycles:%d:%s", outputName,
					kernel.getBufferSize(),
					measurement.getScheme().getWarmCaches() ? "warm" : "cold");
			plotService.plot(plot);
		}*/
		
		// simple plot
		{
			SimplePlot plot = new SimplePlot();

			PerfEventMeasurerOutput.addValues("inst", result, plot);

			plot.setTitle("Load, BufferSize: %d", kernel.getBufferSize());
			plot.setOutputName("%s:inst:%d:%s", outputName,
					kernel.getBufferSize(),
					measurement.getScheme().getWarmCaches() ? "warm" : "cold");
			plotService.plot(plot);
		}
		


	}

}
