package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.MemoryLoadKernelDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventCount;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.SimplePlot;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.MeasurementRepository;
import ch.ethz.ruediste.roofline.measurementDriver.services.PlotService;

import com.google.inject.Inject;

public class MemoryLoadMeasurementController implements IMeasurementController {

	public String getName() {
		return "memoryLoad";
	}

	public String getDescription() {
		return "loads a memory buffer and measures the memory throughput";
	}

	@Inject
	public MeasurementRepository measurementRepository;

	@Inject
	public PlotService plotService;

	public void measure(String outputName) throws IOException {
		MemoryLoadKernelDescription kernel = new
				MemoryLoadKernelDescription();
		// TriadKernelDescription kernel = new TriadKernelDescription();
		kernel.setBufferSize(1024 * 1024 * 10);

		SimpleMeasurementSchemeDescription scheme = new SimpleMeasurementSchemeDescription();
		scheme.setWarmCaches(false);

		PerfEventMeasurerDescription measurer = new
				PerfEventMeasurerDescription();
		measurer.addEvent("event", "core::L2_LINES_IN:SELF:ANY");
		// measurer.addEvent("event", "coreduo::BUS_DRDY_CLOCKS:THIS_AGENT:u");
		// measurer.addEvent("event", "coreduo::INSTRUCTION_RETIRED");
		// measurer.addEvent("event", "coreduo::UNHALTED_CORE_CYCLES");

		// ExecutionTimeMeasurerDescription measurer = new
		// ExecutionTimeMeasurerDescription();

		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(kernel);
		measurement.setScheme(scheme);
		measurement.setMeasurer(measurer);
		kernel.setOptimization("-O3 -msse");

		// perform measurement
		MeasurementResult result = measurementRepository.getMeasurementResults(
				measurement, 40);

		if (false) {
			SimplePlot plot = new SimplePlot();

			PerfEventMeasurerOutput.addValues("event", result, plot);

			plot.setTitle("Load, BufferSize: %d", kernel.getBufferSize());
			plot.setOutputName("%s:event:%d:%s", outputName,
					kernel.getBufferSize(), measurement.getScheme()
							.getWarmCaches() ?
							"warm" : "cold");
			plotService.plot(plot);

		}

		// DescriptiveStatistics statistics = ExecutionTimeMeasurerOutput
		// .getStatistics(result);

		DescriptiveStatistics statistics =
				PerfEventMeasurerOutput.getStatistics("event", result);

		PerfEventMeasurerOutput output = (PerfEventMeasurerOutput) result
				.getOutputs().get(0);
		PerfEventCount eventCount = output.getEventCount("event");
		System.out.printf("%s %s %s %g\n", eventCount.getRawCount(),
				eventCount.getTimeEnabled(), eventCount.getTimeRunning(),
				eventCount.getScaledCount());
		System.out.printf("mem: %g %g\n", statistics.getMin() * 64
				/ (kernel.getBufferSize() * 4),
				statistics.getPercentile(50) / statistics.getMin());
	}
}