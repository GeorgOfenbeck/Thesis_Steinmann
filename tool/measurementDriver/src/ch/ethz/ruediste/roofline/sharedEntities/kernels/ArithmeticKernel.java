package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.sharedEntities.*;

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

	private static final MacroKey mulAddMixMacro = MacroKey.Create(
			"RMT_ARITHMETIC_MUL_ADD_MIX",
			"specifies the mix of multiplications and additions to be used",
			"MUL ADD ADD");

	public enum ArithmeticOperation {
		ArithmeticOperation_ADD, ArithmeticOperation_MUL, ArithmeticOperation_MULADD
	}

	public static final Axis<ArithmeticOperation> arithmeticOperationAxis = new Axis<ArithmeticOperation>(
			"5a393897-bb5a-49ed-898d-1eb62a965ba6", "operation");

	public ArithmeticKernel() {
		setIterations(10000);
	}

	public String getMulAddMix() {
		return getMacroDefinition(mulAddMixMacro);
	}

	public void setMulAddMix(String mulAddMix) {
		setMacroDefinition(mulAddMixMacro, mulAddMix);
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
			"RMT_ARITHMETIC_UNROLL", "number of times to unroll the loop", "4");

	public int getUnroll() {
		return Integer.parseInt(getMacroDefinition(unrollMacro));
	}

	public void setUnroll(int unroll) {
		setMacroDefinition(unrollMacro, Integer.toString(unroll));
	}

	private static final MacroKey dlpMacro = MacroKey
			.Create("RMT_ARITHMETIC_DLP",
					"DataLevelParallelism: number of values that should be computed concurrently",
					"3");

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

		if (coordinate.contains(ArithmeticKernel.arithmeticOperationAxis)) {
			setOperation(coordinate
					.get(ArithmeticKernel.arithmeticOperationAxis));
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

	public static class CreateParameters {
		public ArithmeticOperation operation;
		public InstructionSet instructionSet;

		public CreateParameters(ArithmeticOperation operation,
				InstructionSet instructionSet) {
			this.operation = operation;
			this.instructionSet = instructionSet;
		}
	}

	public static String getSuggestedOptimization(InstructionSet instructionSet) {
		switch (instructionSet) {
		case SSE:
			return "-O3 -msse2";
		case SSEScalar:
			return "-O3 -mfpmath=sse -msse2";
		case x87:
			return "-O3";
		}
		throw new Error("Should not happen");
	}

	@Override
	public String getLabelOverride() {
		String kernelName = null;
		switch (getOperation()) {
		case ArithmeticOperation_ADD:
			kernelName = "ADD";
		break;
		case ArithmeticOperation_MUL:
			kernelName = "MUL";
		break;
		case ArithmeticOperation_MULADD:
			kernelName = "BAL";
		break;

		}
		switch (getInstructionSet()) {
		case SSE:
			kernelName += " SSE";
		break;
		case SSEScalar:
			kernelName += " SSEScalar";
		break;
		case x87:
			kernelName += " x87";
		break;
		}
		return kernelName;
	}
}
