package ch.ethz.ruediste.roofline.measurementDriver.controllers;

import ch.ethz.ruediste.roofline.dom.KernelDescriptionBase;
import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Performance;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.Operation;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;

import com.google.inject.Inject;

public class RooflineController {
	@Inject
	public RooflineService rooflineService;

	private RooflinePlot plot = new RooflinePlot();

	public enum Algorithm {
		Add,
		Mul,
		ArithBalanced,
		Load,
		Store,
		MemBalanced,
	}

	public enum InstructionSet {
		SSE,
		SSEScalar,
		x87,
	}

	public void addPeakPerformance(String name, Algorithm algorithm,
			InstructionSet instructionSet) {
		Performance performance = rooflineService.measurePeakPerformance(
				algorithm,
				instructionSet);

		plot.addPeakPerformance(name, performance);
	}

	private RooflinePoint measureRooflinePoint(String name,
			KernelDescriptionBase kernel, Operation operation,
			MemoryTransferBorder border, ClockType clockType) {
		System.out.printf("Measuring Roofline Point of %s\n", name);

		return new RooflinePoint(name,
				quantityMeasuringService.measureOperationCount(kernel,
						operation),
				quantityMeasuringService
						.measureTransferredBytes(kernel, border),
				quantityMeasuringService
						.measureExecutionTime(kernel, clockType));
	}
}
