package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.dom.Axes.*;
import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

/** Kernel just loading a memory block into memory */
public class MemoryKernel extends MemoryKernelData {

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