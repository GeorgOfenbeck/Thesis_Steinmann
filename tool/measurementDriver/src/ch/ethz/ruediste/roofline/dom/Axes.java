package ch.ethz.ruediste.roofline.dom;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Axis;

public class Axes {
	public static final Axis<MeasurementSchemeDescriptionBase> measurementSchemeAxis = new Axis<MeasurementSchemeDescriptionBase>(
			"scheme");
	public static final Axis<KernelDescriptionBase> kernelAxis = new Axis<KernelDescriptionBase>(
			"kernel");
	public static final Axis<MeasurerDescriptionBase> measurerAxis = new Axis<MeasurerDescriptionBase>(
			"measurer", null, Axis.classNameFormatter);

	public static final Axis<Long> bufferSizeAxis = new Axis<Long>(
			"bufferSize", (long) 1024 * 1024);

	public static final Axis<Long> iterationsAxis = new Axis<Long>(
			"iterations", (long) 1024 * 1024);

	public static final Axis<Integer> unrollAxis = new Axis<Integer>("unroll",
			1);
	public static final Axis<Integer> dlpAxis = new Axis<Integer>("dlp", 1);

	public static final Axis<String> operationAxis = new Axis<String>(
			"operation", "ArithmeticOperation_ADD");

	public static final Axis<String> optimizationAxis = new Axis<String>(
			"optimization", "-O3");
}
