package ch.ethz.ruediste.roofline.sharedEntities;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;

public abstract class KernelBase extends KernelBaseData {

	public void initialize(Coordinate coordinate) {
		if (coordinate.contains(optimizationAxis))
			setOptimization(coordinate.get(optimizationAxis));

		if (coordinate.contains(warmCodeAxis))
			setWarmCode(coordinate.get(warmCodeAxis));

		if (coordinate.contains(warmDataAxis))
			setWarmData(coordinate.get(warmDataAxis));

		if (coordinate.contains(numThreadsAxis)) {
			setNumThreads(coordinate.get(numThreadsAxis));
		}
	}

	public KernelBase() {
		setOptimization("-O3");
		setNumThreads(1);
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

	public String getAdditionalLibraries(SystemInformation systemInformation) {
		return "";
	}

	public String getAdditionalIncludeDirs() {
		return "";
	}

	public TransferredBytes getExpectedTransferredBytes(
			SystemInformation systemInformation) {
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

	protected String getLabelOverride() {
		return StringUtils.removeEnd(getClass().getSimpleName(), "Kernel");
	}

	public final String getLabel() {
		return getLabelOverride() + getLabelSuffix();
	}

	protected String getLabelSuffix() {
		String suffix = "";
		if (getWarmData())
			suffix += "-Data";
		if (getWarmCode())
			suffix += "-Code";
		if (getNumThreads() > 1)
			suffix += "-Threaded";
		return suffix;
	}

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