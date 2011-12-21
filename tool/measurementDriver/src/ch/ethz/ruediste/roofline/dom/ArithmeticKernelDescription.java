package ch.ethz.ruediste.roofline.dom;

public class ArithmeticKernelDescription extends
		ArithmeticKernelDescriptionData {

	/**
	 * name of the operation to be used
	 */
	public static final String operationMacroName = "RMT_ARITHMETIC_OPERATION";

	/**
	 * indicates that the loop should be unrolled by factor 4
	 */
	public static final String unroll4MacroName = "RMT_UNROLL4";
}
