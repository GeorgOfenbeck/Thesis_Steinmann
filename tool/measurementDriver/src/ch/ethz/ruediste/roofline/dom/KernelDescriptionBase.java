package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.dom.Axes.optimizationAxis;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class KernelDescriptionBase extends KernelDescriptionBaseData {

	public void initialize(Coordinate coordinate) {
		setOptimization(coordinate.get(optimizationAxis));
	}

}