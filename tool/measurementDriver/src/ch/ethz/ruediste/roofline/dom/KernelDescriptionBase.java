package ch.ethz.ruediste.roofline.dom;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class KernelDescriptionBase extends KernelDescriptionBaseData {

	public void initialize(Coordinate coordinate) {
		setOptimization(coordinate.get(MeasurementDescription.optimizationAxis));
	}

}