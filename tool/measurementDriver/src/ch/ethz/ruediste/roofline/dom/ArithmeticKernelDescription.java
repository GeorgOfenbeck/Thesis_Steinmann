package ch.ethz.ruediste.roofline.dom;

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
	private static final MacroKey unrollMacro = MacroKey.Create(
			"RMT_ARITHMETIC_UNROLL",
			"number of times to unroll the loop",
			"1");

	public int getUnroll() {
		return Integer.parseInt(getMacroDefinition(unrollMacro));
	}

	public void setUnroll(int unroll) {
		setMacroDefinition(unrollMacro, Integer.toString(unroll));
	}

	private static final MacroKey dlpMacro = MacroKey
			.Create(
					"RMT_ARITHMETIC_DLP",
					"DataLevelParallelism: number of values that should be computed concurrently",
					"1");

	public int getDlp() {
		return Integer.parseInt(getMacroDefinition(dlpMacro));
	}

	public void setDlp(int unroll) {
		setMacroDefinition(dlpMacro, Integer.toString(unroll));
	}

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		setIterations(coordinate.get(MeasurementDescription.iterationsAxis));
		setOperation(coordinate.get(MeasurementDescription.operationAxis));
		setUnroll(coordinate.get(MeasurementDescription.unrollAxis));
		setDlp(coordinate.get(MeasurementDescription.dlpAxis));
	}
}
