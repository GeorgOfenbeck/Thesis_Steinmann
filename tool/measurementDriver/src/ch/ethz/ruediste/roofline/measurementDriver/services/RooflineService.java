package ch.ethz.ruediste.roofline.measurementDriver.services;

import static ch.ethz.ruediste.roofline.dom.Axes.*;
import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController.Algorithm;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController.InstructionSet;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;

import com.google.inject.Inject;

public class RooflineService {
	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public OptimizationService optimizationService;

	public Performance measurePeakPerformance(Algorithm algorithm,
			InstructionSet instructionSet, ClockType clockType) throws Error {
		CoordinateBuilder kernelParameters = new CoordinateBuilder();

		// set the operation for arithmetic kernels
		switch (algorithm) {
		case Add:
			kernelParameters.set(operationAxis, "ArithmeticOperation_ADD");
			break;
		case ArithBalanced:
			kernelParameters.set(operationAxis, "ArithmeticOperation_MULADD");
			break;
		case Mul:
			kernelParameters.set(operationAxis, "ArithmeticOperation_MUL");
			break;
		case Load:
		case MemBalanced:
		case Store:
			throw new Error("Algorithm not supported for peak performance");
		}

		// create the measurement coordinate
		CoordinateBuilder measurementCoordinateBuilder = CoordinateBuilder
				.createCoordinate()
				.set(QuantityMeasuringService.quantityAxis,
						QuantityMeasuringService.Quantity.Performance)
				.set(QuantityMeasuringService.clockTypeAxis, clockType);

		// set the optimization
		switch (instructionSet) {
		case SSE:
			kernelParameters.set(optimizationAxis, "-O3 -msse");
			measurementCoordinateBuilder.set(
					QuantityMeasuringService.operationAxis,
					QuantityMeasuringService.Operation.SSE);
			break;
		case SSEScalar:
			kernelParameters.set(optimizationAxis, "-O3 -mfpmath=sse");
			measurementCoordinateBuilder.set(
					QuantityMeasuringService.operationAxis,
					QuantityMeasuringService.Operation.SSE);
			break;
		case x87:
			kernelParameters.set(optimizationAxis, "-O3");
			measurementCoordinateBuilder.set(
					QuantityMeasuringService.operationAxis,
					QuantityMeasuringService.Operation.x87);
			break;

		}

		// setup optimization space
		ParameterSpace optimzationSpace = new ParameterSpace();
		for (int i = 1; i < 20; i++) {
			optimzationSpace.add(unrollAxis, i);
		}
		for (int i = 1; i < 20; i++) {
			optimzationSpace.add(dlpAxis, i);
		}

		// create the kernel
		ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
		kernel.initialize(kernelParameters.build());

		Coordinate measurementCoordinate = measurementCoordinateBuilder
				.build();

		// do the minimization
		Coordinate bestParams = optimizationService
				.minimize(
						kernel,
						optimzationSpace,
						measurementCoordinate);

		// apply the best parameters
		kernel.initialize(bestParams);

		// measure the performance
		Performance performance = quantityMeasuringService.measurePerformance(
				kernel,
				measurementCoordinate
						.get(QuantityMeasuringService.operationAxis),
				measurementCoordinate
						.get(QuantityMeasuringService.clockTypeAxis));
		return performance;
	}

	public Throughput measurePeakThroughput(Algorithm algorithm,
			MemoryTransferBorder border, ClockType clockType) {
		KernelDescriptionBase kernel = null;
		CoordinateBuilder kernelParameters = new CoordinateBuilder();

		// create the kernel
		switch (algorithm) {
		case Load:
			kernel = new MemoryLoadKernelDescription();
			kernelParameters.set(optimizationAxis, "-O3 -msse");
			kernelParameters.set(bufferSizeAxis, 1024L * 1024 * 10);
			break;
		case Add:
		case ArithBalanced:
		case Mul:
		case MemBalanced:
		case Store:
			throw new Error("Algorithm not supported for peak throughput");
		}

		// initialize the kernel
		kernel.initialize(kernelParameters.build());

		// measure the throughput
		Throughput throughput = quantityMeasuringService.measureThroughput(
				kernel,
				border,
				clockType);
		return throughput;
	}
}
