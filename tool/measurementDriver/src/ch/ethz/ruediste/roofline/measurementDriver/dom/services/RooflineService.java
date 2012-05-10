package ch.ethz.ruediste.roofline.measurementDriver.dom.services;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.ArithmeticKernel.ArithmeticOperation;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MemoryKernel.MemoryOperation;

import com.google.inject.Inject;

public class RooflineService {
	public enum PeakAlgorithm {
		Add, Mul, ArithBalanced, Load, Store, MemBalanced, RandomLoad,
	}

	private static Logger log = Logger.getLogger(RooflineService.class);

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public OptimizationService optimizationService;
	
	@Inject
	public SystemInfoService systemInfoService;

	public Performance measurePeakPerformance(PeakAlgorithm algorithm,
			InstructionSet instructionSet, ClockType clockType) throws Error {
		CoordinateBuilder kernelParameters = new CoordinateBuilder();

		kernelParameters.set(iterationsAxis, 100000L);

		// set the operation for arithmetic kernels
		switch (algorithm) {
		case Add:
			kernelParameters.set(ArithmeticKernel.arithmeticOperationAxis,
					ArithmeticOperation.ArithmeticOperation_ADD);
		break;
		case ArithBalanced:
			kernelParameters.set(ArithmeticKernel.arithmeticOperationAxis,
					ArithmeticOperation.ArithmeticOperation_MULADD);
		break;
		case Mul:
			kernelParameters.set(ArithmeticKernel.arithmeticOperationAxis,
					ArithmeticOperation.ArithmeticOperation_MUL);
		break;
		default:
			throw new Error("PeakAlgorithm not supported for peak performance");
		}

		// create the measurement coordinate
		CoordinateBuilder measurementCoordinateBuilder = CoordinateBuilder
				.createCoordinate()
				.set(QuantityMeasuringService.quantityAxis, Performance.class)
				.set(QuantityMeasuringService.clockTypeAxis, clockType);

		// set the instruction set
		kernelParameters.set(instructionSetAxis, instructionSet);

		// set the optimization
		switch (instructionSet) {
		case SSE:
			kernelParameters.set(optimizationAxis, "-O3 -msse2");
			measurementCoordinateBuilder.set(
					QuantityMeasuringService.operationAxis,
					Operation.DoublePrecisionFlop);
		break;
		case SSEScalar:
			kernelParameters.set(optimizationAxis, "-O3 -mfpmath=sse -msse2");
			measurementCoordinateBuilder.set(
					QuantityMeasuringService.operationAxis,
					Operation.DoublePrecisionFlop);
		break;
		case x87:
			kernelParameters.set(optimizationAxis, "-O3");
			measurementCoordinateBuilder
					.set(QuantityMeasuringService.operationAxis,
							Operation.CompInstr);
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
		ArithmeticKernel kernel = new ArithmeticKernel();
		kernel.initialize(kernelParameters.build());
		
		// setup kernel
		if (algorithm == PeakAlgorithm.ArithBalanced) {
			switch (systemInfoService.getCpuType())
			{
			case Yonah:
				// nothing to do
				break;
			case Core:
				kernel.setMulAddMix("MUL ADD ADD MUL ADD ADD MUL ADD");
				kernel.setMultiplications(5);
				kernel.setAdditions(3);
			}
		}

		Coordinate measurementCoordinate = measurementCoordinateBuilder.build();

		// do the minimization
		Coordinate maximum = optimizationService.maximize(kernel,
				optimzationSpace, measurementCoordinate);

		// apply the best parameters
		kernel.initialize(maximum);
		QuantityCalculator<Performance> calc = quantityMeasuringService
				.getPerformanceCalculator(
						measurementCoordinate
								.get(QuantityMeasuringService.operationAxis),
						measurementCoordinate
								.get(QuantityMeasuringService.clockTypeAxis));
		QuantityMap result = quantityMeasuringService.measureQuantities(kernel,
				calc);

		// measure the performance
		Performance performance = result.best(calc);

		log.info(String.format(
				"peak performance for %s %s %s: parameters: %s value: %f",
				algorithm, instructionSet, clockType, maximum,
				performance.getValue()));
		return performance;
	}

	public Throughput measurePeakThroughput(PeakAlgorithm algorithm,
			MemoryTransferBorder border, ClockType clockType) {
		MemoryKernel kernel = null;
		CoordinateBuilder kernelParameters = new CoordinateBuilder();

		// create the kernel
		switch (algorithm) {
		case Load:
			kernel = new MemoryKernel();
			kernelParameters.set(optimizationAxis, "-O3 -msse");
			kernelParameters.set(bufferSizeAxis, 1024L * 1024 * 5);
			
			// setup kernel
				switch (systemInfoService.getCpuType())
				{
				case Yonah:
					// nothing to do
					break;
				case Core:
					kernel.setUnroll(1);
					kernel.setDlp(1);
				}
		break;
		case RandomLoad:
			kernel = new MemoryKernel();
			kernelParameters.set(optimizationAxis, "-O3");
			kernelParameters.set(bufferSizeAxis, 1024L * 1024L);
			kernelParameters.set(unrollAxis, 50);
			kernelParameters.set(MemoryKernel.memoryOperationAxis,MemoryOperation.MemoryOperation_RandomRead);
		break;
		case Add:
		case ArithBalanced:
		case Mul:
		case MemBalanced:
		case Store:
			throw new Error("PeakAlgorithm not supported for peak throughput");
		}

		// initialize the kernel
		kernel.initialize(kernelParameters.build());
		QuantityCalculator<Throughput> calc = quantityMeasuringService
				.getThroughputCalculator(border,
						clockType);

		QuantityMap result = quantityMeasuringService.measureQuantities(kernel,
				calc);

		// measure the throughput
		Throughput throughput = result.best(calc);
		return throughput;
	}
}
