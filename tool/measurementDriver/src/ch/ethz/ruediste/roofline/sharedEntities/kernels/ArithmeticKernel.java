package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.entities.Axes.*;
import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.Operation;
import ch.ethz.ruediste.roofline.sharedEntities.InstructionSet;

public class ArithmeticKernel extends ArithmeticKernelData {

	/**
	 * name of the operation to be used
	 */

	private static final MacroKey operationMacro = MacroKey.Create(
			"RMT_ARITHMETIC_OPERATION",
			"specifies the arithmetic operation to be used",
			"ArithmeticOperation_ADD");

	private static final MacroKey instructionSetMacro = MacroKey.Create(
			"RMT_ARITHMETIC_INSTRUCTION_SET",
			"specifies the instruction set to be used (x87 or SSE)", "x87");

	public enum ArithmeticOperation {
		ArithmeticOperation_ADD, ArithmeticOperation_MUL, ArithmeticOperation_MULADD
	}

	public ArithmeticKernel() {
		setIterations(10000);
	}

	public ArithmeticOperation getOperation() {
		return ArithmeticOperation.valueOf(getMacroDefinition(operationMacro));
	}

	public void setOperation(ArithmeticOperation operation) {
		setMacroDefinition(operationMacro, operation.toString());
	}

	public InstructionSet getInstructionSet() {
		return Enum.valueOf(InstructionSet.class,
				getMacroDefinition(instructionSetMacro));
	}

	public void setInstructionSet(InstructionSet instructionSet) {
		setMacroDefinition(instructionSetMacro, instructionSet.toString());
	}

	/**
	 * number of times to unroll the loop
	 */

	private static final MacroKey unrollMacro = MacroKey.Create(
			"RMT_ARITHMETIC_UNROLL", "number of times to unroll the loop", "2");

	public int getUnroll() {
		return Integer.parseInt(getMacroDefinition(unrollMacro));
	}

	public void setUnroll(int unroll) {
		setMacroDefinition(unrollMacro, Integer.toString(unroll));
	}

	private static final MacroKey dlpMacro = MacroKey
			.Create("RMT_ARITHMETIC_DLP",
					"DataLevelParallelism: number of values that should be computed concurrently",
					"2");

	public int getDlp() {
		return Integer.parseInt(getMacroDefinition(dlpMacro));
	}

	public void setDlp(int unroll) {
		setMacroDefinition(dlpMacro, Integer.toString(unroll));
	}

	private static final MacroKey additionsMacro = MacroKey
			.Create("RMT_ARITHMETIC_ADDITIONS",
					"Number of additions to do in each iteration of the balanced kernel",
					"2");

	public int getAdditions() {
		return Integer.parseInt(getMacroDefinition(additionsMacro));
	}

	public void setAdditions(int additions) {
		setMacroDefinition(additionsMacro, Integer.toString(additions));
	}

	private static final MacroKey multiplicationsMacro = MacroKey
			.Create("RMT_ARITHMETIC_MULTIPLICATIONS",
					"Number of multiplications to do in each iteration of the balanced kernel",
					"1");

	public int getMultiplications() {
		return Integer.parseInt(getMacroDefinition(multiplicationsMacro));
	}

	public void setMultiplications(int multiplications) {
		setMacroDefinition(multiplicationsMacro,
				Integer.toString(multiplications));
	}

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(iterationsAxis)) {
			setIterations(coordinate.get(iterationsAxis));
		}

		if (coordinate.contains(arithmeticOperationAxis)) {
			setOperation(coordinate.get(arithmeticOperationAxis));
		}

		if (coordinate.contains(unrollAxis)) {
			setUnroll(coordinate.get(unrollAxis));
		}

		if (coordinate.contains(dlpAxis)) {
			setDlp(coordinate.get(dlpAxis));
		}

		if (coordinate.contains(arithBalancedAdditionsAxis)) {
			setAdditions(coordinate.get(arithBalancedAdditionsAxis));
		}

		if (coordinate.contains(arithBalancedMultiplicationsAxis)) {
			setMultiplications(coordinate.get(arithBalancedMultiplicationsAxis));
		}

		if (coordinate.contains(instructionSetAxis)) {
			setInstructionSet(coordinate.get(instructionSetAxis));
		}
	}

	@Override
	public String toString() {
		return String.format("Arithmetic Kernel:%s:%d:unroll %d dlp %d",
				getOperation(), getIterations(), getUnroll(), getDlp());
	}

	@Override
	public Operation getSuggestedOperation() {
		switch (getInstructionSet()) {
		case SSE:
		case SSEScalar:
			return Operation.DoublePrecisionFlop;
		case x87:
			return Operation.CompInstr;
		}
		throw new Error("should not happen");
	}

	@Override
	public OperationCount getExpectedOperationCount() {
		double result = getIterations() * getUnroll() * getDlp();
		if (getOperation() == ArithmeticOperation.ArithmeticOperation_MULADD)
			result *= 2;
		if (getInstructionSet() == InstructionSet.SSE) {
			result *= 2;
		}
		return new OperationCount(result);
	}
}
