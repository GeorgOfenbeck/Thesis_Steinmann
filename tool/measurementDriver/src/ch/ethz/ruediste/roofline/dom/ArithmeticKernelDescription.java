package ch.ethz.ruediste.roofline.dom;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;

public class ArithmeticKernelDescription extends
		ArithmeticKernelDescriptionData {

	/**
	 * name of the operation to be used
	 */
	public static final MacroKey operationMacro = MacroKey.Create(
			"RMT_ARITHMETIC_OPERATION",
			"specifies the arithmetic operation to be used",
			"ArithmeticOperation_ADD");

}
