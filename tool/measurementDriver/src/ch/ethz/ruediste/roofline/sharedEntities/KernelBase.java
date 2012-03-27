package ch.ethz.ruediste.roofline.sharedEntities;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import org.apache.commons.lang.NotImplementedException;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;

public abstract class KernelBase extends KernelBaseData {

	public void initialize(Coordinate coordinate) {
		if (coordinate.contains(optimizationAxis)) {
			setOptimization(coordinate.get(optimizationAxis));
		}
	}

	public KernelBase() {
		setOptimization("-O3");
	}

	/**
	 * returns the name of the kernel without the "Kernel" suffix
	 */
	public String getName() {
		String kernelName = getClass().getSimpleName();
		kernelName = kernelName.substring(0,
				kernelName.length() - "Kernel".length());
		return kernelName;
	}

	public String getAdditionalLibraries() {
		return "";
	}

	public String getAdditionalIncludeDirs() {
		return "";
	}

	public TransferredBytes getExpectedTransferredBytes() {
		throw new NotImplementedException();
	}

	public Operation getSuggestedOperation() {
		throw new NotImplementedException();
	}

	public OperationCount getExpectedOperationCount() {
		throw new NotImplementedException();
	}

	/**
	 * size of the data the kernel operates on
	 */
	public long getDataSize() {
		throw new NotImplementedException();
	}

	public abstract String getLabel();

	public void setOptimizationFromInstructionSet(InstructionSet set) {
		switch (set) {
		case SSE:
			setOptimization("-O3 -msse2");
		break;
		case SSEScalar:
			setOptimization("-O3 -mfpmath=sse -msse2");
		break;
		case x87:
			setOptimization("-O3");
		break;
		}
	}

	public static KernelBase create(Coordinate coordinate) {
		try {
			KernelBase result = coordinate.get(kernelClassAxis)
					.getConstructor()
					.newInstance();
			result.initialize(coordinate);
			return result;
		}
		catch (Exception e) {
			throw new Error(e);
		}
	}
}