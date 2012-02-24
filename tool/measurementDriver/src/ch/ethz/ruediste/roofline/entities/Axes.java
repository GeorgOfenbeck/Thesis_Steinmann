package ch.ethz.ruediste.roofline.entities;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.ClassFinder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Axis;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.ArithmeticKernel.ArithmeticOperation;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MemoryKernel.MemoryOperation;

public class Axes {
	public static final Axis<KernelBase> kernelAxis = new Axis<KernelBase>(
			"a4aa516d-a13c-4ad7-bcde-5176cd3b7bd8", "kernel", null,
			Axis.<KernelBase> classNameFormatter());

	public static final Axis<MeasurerBase> measurerAxis = new Axis<MeasurerBase>(
			"8f18b16a-47e4-467a-9ec1-e5f09bd681d9", "measurer", null,
			Axis.<MeasurerBase> classNameFormatter());

	public static final Axis<Long> bufferSizeAxis = new Axis<Long>(
			"11d6a840-ff93-4095-9f25-26b668f282f9", "bufferSize",
			(long) 1024 * 1024);

	public static final Axis<Long> matrixSizeAxis = new Axis<Long>(
			"5a8c4779-2a7d-4ba3-8a30-311b11df8933", "matrixSize", 128L);

	public static final Axis<Long> blockSizeAxis = new Axis<Long>(
			"9eba3230-c261-469d-9351-9b1cf165dbd4", "blockSize", 8L);

	public static final Axis<Long> iterationsAxis = new Axis<Long>(
			"cff57980-7b2c-463f-9762-7bcfe7a8b565", "iterations", (long) 10000);

	public static final Axis<Integer> unrollAxis = new Axis<Integer>(
			"670c8795-de59-4f56-91f6-cb36f4bde788", "unroll", 1);

	public static final Axis<Integer> dlpAxis = new Axis<Integer>(
			"fec6cd98-a95d-4a3a-86fd-96f3333373ea", "dlp", 1);

	public static final Axis<Integer> arithBalancedAdditionsAxis = new Axis<Integer>(
			"6bfda8c8-c4f0-4ef0-b14f-e00a905c5796", "additions", 1);

	public static final Axis<Integer> arithBalancedMultiplicationsAxis = new Axis<Integer>(
			"096d99fe-f745-414e-86a7-57199f9fd19c", "multiplications", 1);

	public static final Axis<ArithmeticOperation> arithmeticOperationAxis = new Axis<ArithmeticOperation>(
			"5a393897-bb5a-49ed-898d-1eb62a965ba6", "operation",
			ArithmeticOperation.ArithmeticOperation_ADD);

	public static final Axis<MemoryOperation> memoryOperationAxis = new Axis<MemoryOperation>(
			"5542d80a-2f0c-42d2-8e6e-acaf01c4baf8", "operation",
			MemoryOperation.MemoryOperation_READ);

	public static final Axis<String> optimizationAxis = new Axis<String>(
			"c24ed880-fa37-4b82-81ac-690e2b63c560", "optimization", "-O3");

	public static final Axis<InstructionSet> instructionSetAxis = new Axis<InstructionSet>(
			"920f0443-b04e-4254-a53e-c24b0d30e0cc", "instructionSet",
			InstructionSet.x87);

	public static final Axis<ClockType> clockTypeAxis = new Axis<ClockType>(
			"5452e251-7851-437a-a87e-1a9b41c18302", "clockType",
			ClockType.uSecs);

	private static HashMap<UUID, Axis<?>> axes;

	@SuppressWarnings("rawtypes")
	private static HashMap<UUID, Axis<?>> getAxes() {
		if (axes == null) {
			axes = new HashMap<UUID, Axis<?>>();
			List<Pair<Class<?>, Axis>> all = ClassFinder.getStaticFieldValues(
					Axis.class, "ch.ethz.ruediste.roofline");
			for (Pair<Class<?>, Axis> pair : all) {
				axes.put(pair.getRight().getUid(), pair.getRight());
			}
		}
		return axes;
	}

	public static Axis<?> getAxis(String uid) {
		return getAxes().get(UUID.fromString(uid));
	}
}
