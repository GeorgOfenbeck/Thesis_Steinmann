package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;
import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.TransferredBytes;

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

	private static final MacroKey prefetchDistanceMacro = MacroKey.Create(
			"RMT_MEMORY_PREFETCH_DIST", "distance to prefetch", "256");

	public long getPrefetchDistance() {
		return Integer.parseInt(getMacroDefinition(prefetchDistanceMacro));
	}

	public void setPrefetchDistance(long prefetchDistance) {
		setMacroDefinition(prefetchDistanceMacro,
				Long.toString(prefetchDistance));
	}

	public enum PrefetchType {
		_MM_HINT_T0, _MM_HINT_T1, _MM_HINT_T2, _MM_HINT_NTA
	}

	private static final MacroKey prefetchTypeMacro = MacroKey.Create(
			"RMT_MEMORY_PREFETCH_TYPE", "prefetch type", "_MM_HINT_NTA");

	public PrefetchType getPrefetchType() {
		return PrefetchType.valueOf(getMacroDefinition(prefetchTypeMacro));
	}

	public void setPrefetchType(PrefetchType prefetchType) {
		setMacroDefinition(prefetchTypeMacro, prefetchType.toString());
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
		MemoryOperation_READ, MemoryOperation_WRITE, MemoryOperation_RandomRead,
	}

	public MemoryOperation getOperation() {
		return MemoryOperation.valueOf(getMacroDefinition(operationMacro));
	}

	public void setOperation(MemoryOperation operation) {
		setMacroDefinition(operationMacro, operation.toString());
	}

	@Override
	public TransferredBytes getExpectedTransferredBytes() {
		switch (getOperation()) {
		case MemoryOperation_READ:
			return new TransferredBytes(getBufferSize() * getUnroll()
					* getDlp() * 4);
		case MemoryOperation_WRITE:
			return new TransferredBytes(getBufferSize() * getUnroll()
					* getDlp() * 4 * 2);
		default:
			throw new Error("Should not happen");

		}

	}
}