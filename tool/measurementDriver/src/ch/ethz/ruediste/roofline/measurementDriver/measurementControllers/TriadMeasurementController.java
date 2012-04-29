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
		// initialize the roofline plot
		rooflineController.setTitle("Triad");
		rooflineController.addDefaultPeaks();

		for (long size = 10000; size < 100000; size += 10000) {
			// initialize kernel
			TriadKernel kernel = new TriadKernel();
			kernel.setBufferSize(size);
			kernel.setOptimization("-O3");

			// add a roofline point
			rooflineController
					.addRooflinePoint("Triad", size,
							kernel, Operation.CompInstr,
							MemoryTransferBorder.LlcRamBus);

			// create calculators
			QuantityCalculator<Throughput> throughtputCalculator = quantityMeasuringService
					.getThroughputCalculator(MemoryTransferBorder.LlcRamBus,
							ClockType.CoreCycles);

			QuantityCalculator<OperationCount> operationCountCalculator = quantityMeasuringService
					.getOperationCountCalculator(Operation.CompInstr);

			// perform measurement
			QuantityMap result = quantityMeasuringService.measureQuantities(
					kernel, throughtputCalculator, operationCountCalculator);

			// print throughput and operation count
			System.out.printf("size %d: throughput: %s operations: %s\n", size,
					result.best(throughtputCalculator),
					result.best(operationCountCalculator));
		}

		// create the PDF of the plot
		rooflineController.plot();
	}

}
