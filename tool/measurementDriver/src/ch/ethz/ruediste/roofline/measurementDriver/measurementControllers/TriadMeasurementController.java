package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.TriadKernel;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Throughput;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;

import com.google.inject.Inject;

public class TriadMeasurementController implements IMeasurementController {

	public String getName() {
		return "triad";
	}

	public String getDescription() {
		return "runs the triad kernel";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	public void measure(String outputName) throws IOException {

		TriadKernel kernel = new TriadKernel();
		kernel.setBufferSize(1024L * 1024L);
		kernel.setOptimization("-O3 -msse2");

		Throughput throughput = quantityMeasuringService.measureThroughput(
				kernel, MemoryTransferBorder.LlcRam, ClockType.CoreCycles);

		System.out.printf("Throughput :%s\n", throughput);
	}

}
