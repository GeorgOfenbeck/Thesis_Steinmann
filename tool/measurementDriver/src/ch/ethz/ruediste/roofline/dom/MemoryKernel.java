package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.dom.Axes.*;
import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

/** Kernel just loading a memory block into memory */
public class MemoryKernel extends MemoryKernelData {

	private static final MacroKey dlpMacro = MacroKey.Create("RMT_MEMORY_DLP",
			"DataLevelParallelism: buffers to work on concurrently", "2");

	public int getDlp() {
		return Integer.parseInt(getMacroDefinition(dlpMacro));
	}

	public void setDlp(int unroll) {
		setMacroDefinition(dlpMacro, Integer.toString(unroll));
	}

	private static final MacroKey unrollMacro = MacroKey.Create(
			"RMT_MEMORY_UNROLL", "number of times to unroll the loop", "2");

	public int getUnroll() {
		return Integer.parseInt(getMacroDefinition(unrollMacro));
	}

	public void setUnroll(int unroll) {
		setMacroDefinition(unrollMacro, Integer.toString(unroll));
	}

	private static final MacroKey operationMacro = MacroKey
			.Create("RMT_MEMORY_OPERATION",
					"specifies the memory operation to be used",
					"MemoryOperation_READ");

	@Override
	public void initialize(Coordinate coordinate) {
		super.initialize(coordinate);
		if (coordinate.contains(bufferSizeAxis))
			setBufferSize(coordinate.get(bufferSizeAxis));

		if (coordinate.contains(memoryOperationAxis))
			setOperation(coordinate.get(memoryOperationAxis));

		if (coordinate.contains(iterationsAxis))
			setIterations(coordinate.get(iterationsAxis));

		if (coordinate.contains(dlpAxis))
			setDlp(coordinate.get(dlpAxis));

		if (coordinate.contains(unrollAxis))
			setUnroll(coordinate.get(unrollAxis));
	}

	public enum MemoryOperation {
		MemoryOperation_READ, MemoryOperation_WRITE,
	}

	public MemoryOperation getOperation() {
		return MemoryOperation.valueOf(getMacroDefinition(operationMacro));
	}

	public void setOperation(MemoryOperation operation) {
		setMacroDefinition(operationMacro, operation.toString());
	}
}