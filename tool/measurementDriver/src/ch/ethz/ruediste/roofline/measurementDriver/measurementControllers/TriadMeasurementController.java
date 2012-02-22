package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.TriadKernel;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.Operation;

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

	@Inject
	RooflineController rooflineController;

	public void measure(String outputName) throws IOException {

		rooflineController.setTitle("Triad");
		rooflineController.addDefaultPeaks();

		for (long size = 10000; size < 100000; size += 10000) {
			TriadKernel kernel = new TriadKernel();
			kernel.setBufferSize(size);
			kernel.setOptimization("-O3 -msse2");

			rooflineController.addRooflinePoint("Triad", Long.toString(size),
					kernel, Operation.DoublePrecisionFlop,
					MemoryTransferBorder.LlcRam);

			Throughput throughput = quantityMeasuringService.measureThroughput(
					kernel, MemoryTransferBorder.LlcRam, ClockType.CoreCycles);

			OperationCount operations = quantityMeasuringService
					.measureOperationCount(kernel, Operation.SSEFlop);

			System.out.printf("size %d: throughput: %s operations: %s\n", size,
					throughput, operations);
		}

		rooflineController.plot();
	}

}
