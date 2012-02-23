package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.Axes.*;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.MemoryKernel.MemoryOperation;
import ch.ethz.ruediste.roofline.dom.MemoryKernel.PrefetchType;
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
		Axis<Long> prefetchDistanceAxis = new Axis<Long>(
				"a1242032-756e-4d56-b0f1-4f9c63e6b2a9", "prefetchDistance");
		Axis<PrefetchType> prefetchTypeAxis = new Axis<PrefetchType>(
				"b7d2b922-dfba-4c76-bdf2-65108a6b4a29", "prefetchType");

		ParameterSpace space = new ParameterSpace();
		space.add(iterationsAxis, 1L);

		space.add(memoryOperationAxis, MemoryOperation.MemoryOperation_READ);
		//space.add(memoryOperationAxis, MemoryOperation.MemoryOperation_WRITE);

		space.add(optimizationAxis, "-O3 -msse2");

		for (int i = 1; i <= 1; i++) {
			space.add(dlpAxis, i);
		}
		for (int i = 1; i <= 4; i++) {
			space.add(unrollAxis, i);
		}

		space.add(prefetchDistanceAxis, 0L);
		for (long i = 64; i <= 2048; i*=2) {
			space.add(prefetchDistanceAxis, i);
		}

		for (PrefetchType type : PrefetchType.values()) {
			space.add(prefetchTypeAxis, type);
		}

		space.add(bufferSizeAxis, 1024L * 1024L);

		for (Coordinate coordinate : space.getAllPoints(space
				.getAllAxesWithLeastSignificantAxes(optimizationAxis,
						memoryOperationAxis, iterationsAxis

						))) {
			MemoryKernel kernel = new MemoryKernel();
			kernel.setPrefetchDistance(coordinate.get(prefetchDistanceAxis));
			kernel.setPrefetchType(coordinate.get(prefetchTypeAxis));

			kernel.initialize(coordinate);

			Throughput throughput = quantityMeasuringService.measureThroughput(
					kernel, MemoryTransferBorder.LlcRam, ClockType.CoreCycles);
			//System.out.printf("throughput %s: %s\n", coordinate, throughput);

			TransferredBytes transferredBytes = quantityMeasuringService
					.measureTransferredBytes(kernel,
							MemoryTransferBorder.LlcRam);

			System.out.printf("%s: throughput: %s Transferred bytes: %s\n",
					coordinate.toString(memoryOperationAxis, bufferSizeAxis,
							dlpAxis, unrollAxis, prefetchDistanceAxis,
							prefetchTypeAxis), throughput, transferredBytes);
		}
	}
}
