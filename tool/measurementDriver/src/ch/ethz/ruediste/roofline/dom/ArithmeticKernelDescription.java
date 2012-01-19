package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.dom.Axes.*;
import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class ArithmeticKernelDescription extends
		ArithmeticKernelDescriptionData {

	/**
	 * name of the operation to be used
	 */

	private static final MacroKey operationMacro = MacroKey.Create(
			"RMT_ARITHMETIC_OPERATION",
			"specifies the arithmetic operation to be used",
			"ArithmeticOperation_ADD");

	public String getOperation() {
		return getMacroDefinition(operationMacro);
	}

	public void setOperation(String operation) {
		setMacroDefinition(operationMacro, operation);
	}

	/**
	 * number of times to unroll the loop
	 */
	/*
	 * private static final MacroKey unrollMacro = MacroKey.Create(
	 * "RMT_ARITHMETIC_UNROLL", "number of times to unroll the loop", "1");
	 * 
	 * public int getUnroll() { return
	 * Integer.parseInt(getMacroDefinition(unrollMacro)); }
	 * 
	 * public void setUnroll(int unroll) { setMacroDefinition(unrollMacro,
	 * Integer.toString(unroll)); }
	 * 
	 * private static final MacroKey dlpMacro = MacroKey .Create(
	 * "RMT_ARITHMETIC_DLP",
	 * "DataLevelParallelism: number of values that should be computed concurrently"
	 * , "1");
	 * 
	 * public int getDlp() { return
	 * Integer.parseInt(getMacroDefinition(dlpMacro)); }
	 * 
	 * public void setDlp(int unroll) { setMacroDefinition(dlpMacro,
	 * Integer.toString(unroll)); }
	 */

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(iterationsAxis))
			setIterations(coordinate.get(iterationsAxis));

		if (coordinate.contains(operationAxis))
			setOperation(coordinate.get(operationAxis));

		if (coordinate.contains(unrollAxis))
			setUnroll(coordinate.get(unrollAxis));

		if (coordinate.contains(dlpAxis))
			setDlp(coordinate.get(dlpAxis));
	}
}
