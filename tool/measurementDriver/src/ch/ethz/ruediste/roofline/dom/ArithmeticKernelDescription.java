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

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		setIterations(coordinate.get(MeasurementDescription.iterationsAxis));
		setOperation(coordinate.get(MeasurementDescription.operationAxis));
		setUnroll(coordinate.get(MeasurementDescription.unrollAxis));
	}
}
