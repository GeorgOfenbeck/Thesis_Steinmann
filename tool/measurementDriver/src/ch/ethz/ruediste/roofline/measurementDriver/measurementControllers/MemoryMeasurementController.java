package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.RooflineService.PeakAlgorithm;
import ch.ethz.ruediste.roofline.sharedEntities.ClockType;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MemoryKernel.MemoryOperation;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MemoryKernel.PrefetchType;

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

	@Inject
	public RooflineService rooflineService;

	public void measure(String outputName) throws IOException {
		System.out.printf("peak tp: %f\n", rooflineService
				.measurePeakThroughput(PeakAlgorithm.Load,
						MemoryTransferBorder.LlcRamBus, ClockType.CoreCycles)
				.getValue());

		Axis<Long> prefetchDistanceAxis = new Axis<Long>(
				"a1242032-756e-4d56-b0f1-4f9c63e6b2a9", "prefetchDistance");
		Axis<PrefetchType> prefetchTypeAxis = new Axis<PrefetchType>(
				"b7d2b922-dfba-4c76-bdf2-65108a6b4a29", "prefetchType");

		ParameterSpace space = new ParameterSpace();
		space.add(iterationsAxis, 1L);

		space.add(MemoryKernel.memoryOperationAxis,
				MemoryOperation.MemoryOperation_READ);
		/*space.add(MemoryKernel.memoryOperationAxis,
				MemoryOperation.MemoryOperation_WRITE);*/
		/*space.add(MemoryKernel.memoryOperationAxis,
				MemoryOperation.MemoryOperation_RandomRead);*/

		space.add(optimizationAxis, "-O3 -msse2");
		/*
			

				space.add(prefetchDistanceAxis, 0L);
				for (long i = 64; i <= 2048; i *= 2) {
					space.add(prefetchDistanceAxis, i);
				}

				for (PrefetchType type : PrefetchType.values()) {
					space.add(prefetchTypeAxis, type);
				}*/
		for (int i = 1; i <= 2; i++) {
			space.add(dlpAxis, i);
		}

		for (int i = 1; i <= 4; i++) {
			space.add(unrollAxis, i);
		}

		space.add(bufferSizeAxis, 1024L * 1024L * 10);

		for (Coordinate coordinate : space.getAllPoints(null, optimizationAxis,
				MemoryKernel.memoryOperationAxis, iterationsAxis)) {
			MemoryKernel kernel = new MemoryKernel();

			//kernel.setPrefetchDistance(coordinate.get(prefetchDistanceAxis));
			//kernel.setPrefetchType(coordinate.get(prefetchTypeAxis));

			kernel.initialize(coordinate);
			QuantityCalculator<Throughput> calc = quantityMeasuringService.getThroughputCalculator(MemoryTransferBorder.LlcRamBus,
					ClockType.CoreCycles);
			
			QuantityMap result = quantityMeasuringService.measureQuantities(kernel, calc);

			Throughput throughput = result.best(calc);
			QuantityCalculator<TransferredBytes> calculator = quantityMeasuringService.getTransferredBytesCalculator(MemoryTransferBorder.LlcRamBus);
			QuantityMap result1 = quantityMeasuringService.measureQuantities(kernel, calculator);

			TransferredBytes transferredBytes = result1.best(calculator);

			System.out.printf("%s: throughput: %s Transferred bytes: %s\n",
					coordinate.toString(),
					throughput, transferredBytes);
		}

	}
}
