package ch.ethz.ruediste.roofline.sharedEntities;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Axis;

public class Axes {
	public static final Axis<KernelBase> kernelAxis = new Axis<KernelBase>(
			"a4aa516d-a13c-4ad7-bcde-5176cd3b7bd8", "kernel",
			Axis.<KernelBase> classNameFormatter());

	public static final Axis<Class<? extends KernelBase>> kernelClassAxis = new Axis<Class<? extends KernelBase>>(
			"812e7168-f157-453a-86f6-9576900a7834", "kernelClass",
			Axis.<KernelBase> clazzNameFormatter());

	public static final Axis<MeasurerBase> measurerAxis = new Axis<MeasurerBase>(
			"8f18b16a-47e4-467a-9ec1-e5f09bd681d9", "measurer",
			Axis.<MeasurerBase> classNameFormatter());

	public static final Axis<Long> bufferSizeAxis = new Axis<Long>(
			"11d6a840-ff93-4095-9f25-26b668f282f9", "bufferSize");

	public static final Axis<Long> matrixSizeAxis = new Axis<Long>(
			"5a8c4779-2a7d-4ba3-8a30-311b11df8933", "matrixSize");

	public static final Axis<Long> blockSizeAxis = new Axis<Long>(
			"9eba3230-c261-469d-9351-9b1cf165dbd4", "blockSize");

	public static final Axis<Long> iterationsAxis = new Axis<Long>(
			"cff57980-7b2c-463f-9762-7bcfe7a8b565", "iterations");

	public static final Axis<Integer> unrollAxis = new Axis<Integer>(
			"670c8795-de59-4f56-91f6-cb36f4bde788", "unroll");

	public static final Axis<Integer> dlpAxis = new Axis<Integer>(
			"fec6cd98-a95d-4a3a-86fd-96f3333373ea", "dlp");

	public static final Axis<Integer> arithBalancedAdditionsAxis = new Axis<Integer>(
			"bf11e6c9-6675-4ad4-b9e4-c41dfd6ec17c", "additions");

	public static final Axis<Integer> arithBalancedMultiplicationsAxis = new Axis<Integer>(
			"096d99fe-f745-414e-86a7-57199f9fd19c", "multiplications");

	public static final Axis<String> optimizationAxis = new Axis<String>(
			"c24ed880-fa37-4b82-81ac-690e2b63c560", "optimization");

	public static final Axis<InstructionSet> instructionSetAxis = new Axis<InstructionSet>(
			"920f0443-b04e-4254-a53e-c24b0d30e0cc", "instructionSet");

	public static final Axis<ClockType> clockTypeAxis = new Axis<ClockType>(
			"5452e251-7851-437a-a87e-1a9b41c18302", "clockType");

	public static final Axis<Boolean> warmCodeAxis = new Axis<Boolean>(
			"f34ff279-faa1-46ac-b284-868ac0e62843", "warmCode");
	public static final Axis<Boolean> warmDataAxis = new Axis<Boolean>(
			"fc1fc977-5ab4-4ac5-8a04-991fbd8554ac", "warmData");

}
