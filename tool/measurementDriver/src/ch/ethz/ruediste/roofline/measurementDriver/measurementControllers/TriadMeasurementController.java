package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.TriadKernel;

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
			kernel.setOptimization("-O3");

			rooflineController.addRooflinePoint("Triad", Long.toString(size),
					kernel, Operation.CompInstr, MemoryTransferBorder.LlcRamBus);
			QuantityCalculator<Throughput> calc = quantityMeasuringService.getThroughputCalculator(MemoryTransferBorder.LlcRamBus,
					ClockType.CoreCycles);
			
			QuantityMap result = quantityMeasuringService.measureQuantities(kernel, calc);

			Throughput throughput = result.min(calc);
			QuantityCalculator<OperationCount> calculator = quantityMeasuringService.getOperationCountCalculator(Operation.CompInstr);
			QuantityMap result1 = quantityMeasuringService.measureQuantities(kernel, calculator);

			OperationCount operations = result1.min(calculator);

			System.out.printf("size %d: throughput: %s operations: %s\n", size,
					throughput, operations);
		}

		rooflineController.plot();
	}

}
