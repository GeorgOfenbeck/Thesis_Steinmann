package ch.ethz.ruediste.roofline.sharedEntities;

import static ch.ethz.ruediste.roofline.entities.Axes.optimizationAxis;

import org.apache.commons.lang.NotImplementedException;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.TransferredBytes;

public class KernelBase extends KernelBaseData {

	public void initialize(Coordinate coordinate) {
		if (coordinate.contains(optimizationAxis)) {
			setOptimization(coordinate.get(optimizationAxis));
		}
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
}