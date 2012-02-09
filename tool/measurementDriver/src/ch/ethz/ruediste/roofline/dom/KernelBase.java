package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.dom.Axes.optimizationAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class KernelBase extends KernelBaseData {

	public void initialize(Coordinate coordinate) {
		if (coordinate.contains(optimizationAxis)) {
			setOptimization(coordinate.get(optimizationAxis));
		}
	}

}