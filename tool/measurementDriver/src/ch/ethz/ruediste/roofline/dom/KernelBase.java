package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.dom.Axes.optimizationAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

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
}