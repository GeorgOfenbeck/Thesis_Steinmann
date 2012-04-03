package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.TransferredBytes;
import ch.ethz.ruediste.roofline.sharedEntities.*;

/** Kernel just loading a memory block into memory */
public class MemoryKernel extends MemoryKernelData {

	private static final MacroKey dlpMacro = MacroKey.Create("RMT_MEMORY_DLP",
			"DataLevelParallelism: buffers to work on concurrently", "2");
	public static final Axis<MemoryOperation> memoryOperationAxis = new Axis<MemoryOperation>(
			"5542d80a-2f0c-42d2-8e6e-acaf01c4baf8", "operation");

	public int getDlp() {
		return Integer.parseInt(getMacroDefinition(dlpMacro));
	}

	public void setDlp(int unroll) {
		setMacroDefinition(dlpMacro, Integer.toString(unroll));
	}

	private static final MacroKey prefetchDistanceMacro = MacroKey.Create(
			"RMT_MEMORY_PREFETCH_DIST", "distance to prefetch", "0");

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

		if (coordinate.contains(MemoryKernel.memoryOperationAxis))
			setOperation(coordinate.get(MemoryKernel.memoryOperationAxis));

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
	public long getDataSize() {
		return getBufferSize() * getUnroll() * getDlp() * 4;
	}

	@Override
	public TransferredBytes getExpectedTransferredBytes(
			SystemInformation systemInformation) {
		long bufferBytes = getDataSize();
		switch (getOperation()) {
		case MemoryOperation_READ:
			return new TransferredBytes(bufferBytes);
		case MemoryOperation_WRITE:
			long cacheSize = systemInformation.LLCCacheSize;
			return new TransferredBytes(bufferBytes // read
					+ Math.max(0, bufferBytes - cacheSize)); // write, taking write back into account
		default:
			throw new Error("Should not happen");

		}

	}

	@Override
	public String getLabel() {
		switch (getOperation()) {
		case MemoryOperation_READ:
			return "Read";
		case MemoryOperation_RandomRead:
			return "Random";
		case MemoryOperation_WRITE:
			return "Write";
		}
		throw new Error("should not happen");
	}
}