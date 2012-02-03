package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.Axes.*;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.MemoryKernelDescription.MemoryOperation;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;

import com.google.inject.Inject;

public class MemoryMeasurementController implements IMeasurementController {

	public String getName() {
		return "memory";
	}

	public String getDescription() {
		return "runs the memory kernel";
	}

	@Inject
	MeasurementAppController measurementAppController;

	@Inject
	public PlotService plotService;

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	public void measure(String outputName) throws IOException {
		ParameterSpace space = new ParameterSpace();
		space.add(iterationsAxis, 1L);

		space.add(memoryOperationAxis, MemoryOperation.MemoryOperation_READ);
		space.add(memoryOperationAxis, MemoryOperation.MemoryOperation_WRITE);

		space.add(optimizationAxis, "-O3 -msse2");

		space.add(bufferSizeAxis, 1024L * 1024 * 10);

		for (Coordinate coordinate : space.getAllPoints(space
				.getAllAxesWithLeastSignificantAxes(optimizationAxis,
						memoryOperationAxis, iterationsAxis

				))) {
			MemoryKernelDescription kernel = new MemoryKernelDescription();
			kernel.initialize(coordinate);

			Throughput throughput = quantityMeasuringService.measureThroughput(
					kernel, MemoryTransferBorder.LlcRam, ClockType.CoreCycles);
			System.out.printf("performance %s: %s\n", coordinate, throughput);

			TransferredBytes transferredBytes = quantityMeasuringService
					.measureTransferredBytes(kernel,
							MemoryTransferBorder.LlcRam);
			System.out.printf("transferred bytes %s: %s\n", coordinate,
					transferredBytes);
		}
	}
}
