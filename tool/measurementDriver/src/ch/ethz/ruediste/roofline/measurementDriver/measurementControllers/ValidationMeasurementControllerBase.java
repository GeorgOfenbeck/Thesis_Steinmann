package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.head;
import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.util.Instantiator;
import ch.ethz.ruediste.roofline.sharedEntities.InstructionSet;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.ArithmeticKernel.ArithmeticOperation;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MemoryKernel.MemoryOperation;

import com.google.inject.Inject;

public class ValidationMeasurementControllerBase {

	public static ConfigurationKey<Boolean> fastKey = ConfigurationKey.Create(
			Boolean.class, "fast", "reduce proble sizes", false);

	@Inject
	public Configuration configuration;

	@Inject
	public Instantiator instantiator;

	@Inject
	public SystemInfoService systemInfoService;

	@Inject
	public OptimizationService optimizationService;

	public ValidationMeasurementControllerBase() {
		super();
	}

	public Coordinate createReadKernelCoordinate() {
		CoordinateBuilder coord = new CoordinateBuilder();
		coord.set(kernelClassAxis, MemoryKernel.class);
		coord.set(unrollAxis, 2);
		coord.set(dlpAxis, 2);
		coord.set(optimizationAxis, "-O3 -msse2");
		coord.set(MemoryKernel.memoryOperationAxis,
				MemoryOperation.MemoryOperation_READ);
		return coord.build();
	}

	public Coordinate createWriteKernelCoordinate(boolean useSSE,
			boolean useStreamingStore) {
		CoordinateBuilder coord = new CoordinateBuilder();
		coord.set(kernelClassAxis, MemoryKernel.class);
		coord.set(unrollAxis, 2);
		coord.set(dlpAxis, 1);
		coord.set(MemoryKernel.useStreamingStoreAxis, useStreamingStore);
		coord.set(optimizationAxis, useSSE ? "-O3 -msse2" : "-O3");
		coord.set(MemoryKernel.memoryOperationAxis,
				MemoryOperation.MemoryOperation_WRITE);
		return coord.build();
	}

	public Coordinate createTriadKernelCoordinate() {
		CoordinateBuilder coord = new CoordinateBuilder();
		coord.set(kernelClassAxis, TriadKernel.class);
		coord.set(optimizationAxis, "-O3");
		return coord.build();
	}

	public Coordinate createArithKernelCoordinate(
			ArithmeticOperation operation, InstructionSet instructionSet) {
		CoordinateBuilder coord = new CoordinateBuilder();
		coord.set(kernelClassAxis,
				ArithmeticKernel.class);
		coord.set(ArithmeticKernel.arithmeticOperationAxis,
				operation);
		coord.set(instructionSetAxis,
				instructionSet);
		coord.set(optimizationAxis,
				ArithmeticKernel.getSuggestedOptimization(instructionSet));

		switch (systemInfoService.getCpuType()){
		case Core:
			if (operation==ArithmeticOperation.ArithmeticOperation_MUL)
			coord.set(dlpAxis, 10);
		}
		/*// setup optimization space
		ParameterSpace optimzationSpace = new ParameterSpace();
		for (int i = 1; i < 20; i++) {
			optimzationSpace.add(unrollAxis, i);
		}
		for (int i = 1; i < 20; i++) {
			optimzationSpace.add(dlpAxis, i);
		}

		KernelBase kernel = KernelBase.create(coord.build());

		CoordinateBuilder measurementCoordinateBuilder = new CoordinateBuilder();
		measurementCoordinateBuilder.set(QuantityMeasuringService.quantityAxis,
				Performance.class);
		measurementCoordinateBuilder.set(
				QuantityMeasuringService.clockTypeAxis, ClockType.CoreCycles);
		measurementCoordinateBuilder
				.set(QuantityMeasuringService.operationAxis,
						kernel.getSuggestedOperation());

		// do the minimization
		Coordinate maximum = optimizationService.maximize(kernel,
				optimzationSpace, measurementCoordinateBuilder.build());

		coord.set(unrollAxis, maximum.get(unrollAxis));
		coord.set(dlpAxis, maximum.get(dlpAxis));*/
		return coord.build();
	}

	public Coordinate[] createArithKernelCoordinates() {
		return new Coordinate[] {
				createArithKernelCoordinate(
						ArithmeticOperation.ArithmeticOperation_ADD,
						InstructionSet.SSE),
				createArithKernelCoordinate(
						ArithmeticOperation.ArithmeticOperation_ADD,
						InstructionSet.x87),
				createArithKernelCoordinate(
						ArithmeticOperation.ArithmeticOperation_MUL,
						InstructionSet.SSE),
				createArithKernelCoordinate(
						ArithmeticOperation.ArithmeticOperation_MUL,
						InstructionSet.x87) };
	}

	public Coordinate[] createMemKernelCoordinates() {
		return new Coordinate[] {
				createReadKernelCoordinate(),
				createWriteKernelCoordinate(true, false),
				createWriteKernelCoordinate(true, true),
				createTriadKernelCoordinate() };
	}

	/**
	 * @return
	 */
	protected List<Integer> cpuSingletonList() {
		List<Integer> singleCpu = Collections
				.singletonList(head(systemInfoService
						.getOnlineCPUs()));
		return singleCpu;
	}
}