package ch.ethz.ruediste.roofline.measurementDriver.services;

import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.dom.KernelDescriptionBase;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.measurementDriver.util.IBinaryPredicate;

import com.google.inject.Inject;

public class OptimizationService {

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	public Pair<Coordinate, Quantity> minimize(KernelDescriptionBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint) {
		return optimize(kernel, optimizationSpace, measurementPoint,
				Quantity.lessThan);
	}

	public Pair<Coordinate, Quantity> maximize(KernelDescriptionBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint) {
		return optimize(kernel, optimizationSpace, measurementPoint,
				Quantity.moreThan);
	}

	public Pair<Coordinate, Quantity> optimize(KernelDescriptionBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint,
			IBinaryPredicate<Quantity, Quantity> betterThan) {

		Coordinate bestCoordinate = null;
		Quantity bestValue = null;

		for (Coordinate coordinate : optimizationSpace) {
			kernel.initialize(coordinate);

			Quantity result = quantityMeasuringService.measure(kernel,
					measurementPoint);

			if (bestValue == null || betterThan.apply(result, bestValue)) {
				bestValue = result;
				bestCoordinate = coordinate;
			}
		}
		return Pair.of(bestCoordinate, bestValue);
	}

}
