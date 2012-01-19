package ch.ethz.ruediste.roofline.measurementDriver.services;

import ch.ethz.ruediste.roofline.dom.KernelDescriptionBase;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

import com.google.inject.Inject;

public class OptimizationService {

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	public Coordinate minimize(KernelDescriptionBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint) {

		Coordinate minCoordinate = null;
		double minValue = Double.MAX_VALUE;

		for (Coordinate coordinate : optimizationSpace) {
			kernel.initialize(coordinate);

			Quantity result = quantityMeasuringService.measure(kernel,
					measurementPoint);

			if (result.getValue() < minValue) {
				minValue = result.getValue();
				minCoordinate = coordinate;
			}
		}

		return minCoordinate;
	}
}
