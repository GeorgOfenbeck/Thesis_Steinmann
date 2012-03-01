package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.util.*;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.OptimizationRepository;
import ch.ethz.ruediste.roofline.measurementDriver.util.IBinaryPredicate;
import ch.ethz.ruediste.roofline.sharedEntities.KernelBase;

import com.google.inject.Inject;

/**
 * the optimization service optimizes some parameters for a given kernel
 */
public class OptimizationService {
	private static Logger log = Logger.getLogger(OptimizationService.class);

	public static ConfigurationKey<Boolean> reoptimizeKey = ConfigurationKey
			.Create(Boolean.class, "reoptimize",
					"if set to true, redo the optimizations", false);

	public static ConfigurationKey<Boolean> fastOptimizeKey = ConfigurationKey
			.Create(Boolean.class, "optimize.fast",
					"if set to true, do a fast optimization", true);

	public enum Comparison {
		lessThan, moreThan
	}

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public OptimizationRepository optimizationRepository;

	@Inject
	public Configuration configuration;

	/**
	 * do a minimization of the measurement result
	 * 
	 * @param kernel
	 *            kernel to optimize
	 * @param optimizationSpace
	 *            kernel parameters to try
	 * @param measurementPoint
	 *            measurement parameters
	 * @return coordinate within the optimizationSpace which gives the lowest
	 *         measurement result
	 */
	public Coordinate minimize(KernelBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint) {
		return optimize(kernel, optimizationSpace, measurementPoint,
				Comparison.lessThan);
	}

	/**
	 * do a maximization of the measurement result
	 * 
	 * @param kernel
	 *            kernel to optimize
	 * @param optimizationSpace
	 *            kernel parameters to try
	 * @param measurementPoint
	 *            measurement parameters
	 * @return coordinate within the optimizationSpace which gives the highest
	 *         measurement result
	 */
	public Coordinate maximize(KernelBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint) {
		return optimize(kernel, optimizationSpace, measurementPoint,
				Comparison.moreThan);
	}

	/**
	 * do an optimization of the measurement result
	 * 
	 * @param kernel
	 *            kernel to optimize
	 * @param optimizationSpace
	 *            kernel parameters to try
	 * @param measurementPoint
	 *            measurement parameters
	 * @param comparison
	 *            comparison to be used
	 * @return coordinate within the optimizationSpace which gives the highest
	 *         measurement result
	 */
	public Coordinate optimize(KernelBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint,
			Comparison comparison) {

		Coordinate bestCoordinate = null;
		String hash = optimizationRepository.getHash(kernel, optimizationSpace,
				measurementPoint, comparison);
		if (!configuration.get(reoptimizeKey)) {
			log.trace("trying to load result from cache");

			// load result from cache
			bestCoordinate = optimizationRepository.getOptimum(hash);
		}

		if (bestCoordinate == null) {
			log.info("Optimizing " + kernel);
			// get comparator
			IBinaryPredicate<Quantity<?>, Quantity<?>> betterThan;
			switch (comparison) {
			case lessThan:
				betterThan = Quantity.lessThanUntyped();
			break;
			case moreThan:
				betterThan = Quantity.moreThanUntyped();
			break;
			default:
				throw new Error("should not happen");
			}

			if (configuration.get(fastOptimizeKey)) {
				bestCoordinate = optimizeFast(kernel, optimizationSpace,
						measurementPoint, betterThan);
			}
			else {
				bestCoordinate = optimizeFull(kernel, optimizationSpace,
						measurementPoint, betterThan);
			}

			// store result
			optimizationRepository.storeOptimum(bestCoordinate, hash);
		}
		return bestCoordinate;
	}

	public Coordinate optimizeFull(KernelBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint,
			IBinaryPredicate<Quantity<?>, Quantity<?>> betterThan) {
		Coordinate bestCoordinate = null;
		Quantity<?> bestValue = null;

		// explore optimization space
		List<Coordinate> points = optimizationSpace.getAllPoints();
		int i = 0;
		for (Coordinate coordinate : points) {
			i++;
			log.info(String.format("Optimizing point %d of %d", i,
					points.size()));
			// initialize kernel
			kernel.initialize(coordinate);

			Quantity<?> result = quantityMeasuringService.measure(kernel,
					measurementPoint);

			if (bestValue == null || betterThan.apply(result, bestValue)) {
				bestValue = result;
				bestCoordinate = coordinate;
			}
		}
		return bestCoordinate;
	}

	public Coordinate optimizeFast(KernelBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint,
			IBinaryPredicate<Quantity<?>, Quantity<?>> betterThan) {
		Coordinate bestCoordinate = null;
		Quantity<?> bestValue = null;

		// explore optimization space
		List<Coordinate> points = optimizationSpace.getAllPoints();
		Collections.shuffle(points, new Random(0));

		int longestSide = optimizationSpace.getAxisLength(optimizationSpace
				.getLongestAxis());

		int maxSteps = 2; //longestSide;

		int startPoints = Math.min(5, points.size());

		for (int i = 0; i < startPoints; i++) {
			Coordinate currentPoint = points.get(i);
			log.debug("starting at " + currentPoint);
			// measure
			kernel.initialize(currentPoint);
			Quantity<?> currentValue = quantityMeasuringService.measure(kernel,
					measurementPoint);

			for (int step = 0; step < maxSteps; step++) {
				Coordinate bestNeighbor = null;
				Quantity<?> bestNeighborValue = null;
				// find best neighbor
				for (Coordinate neighbor : optimizationSpace
						.getNeighbors(currentPoint)) {
					// measure neighbor
					kernel.initialize(neighbor);
					Quantity<?> neighborValue = quantityMeasuringService
							.measure(kernel, measurementPoint);
					if (bestNeighbor == null
							|| betterThan.apply(neighborValue,
									bestNeighborValue)) {
						bestNeighbor = neighbor;
						bestNeighborValue = neighborValue;
					}
				}

				// check if a neighbor was better
				if (betterThan.apply(bestNeighborValue, currentValue)) {
					currentValue = bestNeighborValue;
					currentPoint = bestNeighbor;
					log.debug("moved to " + currentPoint);
				}
				else {
					// break if no better neighbor has been found
					break;
				}
			}

			if (bestValue == null || betterThan.apply(currentValue, bestValue)) {
				bestValue = currentValue;
				bestCoordinate = currentPoint;
			}
		}

		return bestCoordinate;
	}

}
