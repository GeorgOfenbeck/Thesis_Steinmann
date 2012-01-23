package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.KernelDescriptionBase;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.OptimizationService.Comparison;

import com.google.inject.Inject;

public class OptimizationRepository {
	private static Logger log = Logger.getLogger(OptimizationRepository.class);
	@Inject
	HashService hashService;

	@Inject
	CacheService cacheService;

	@Inject
	public Configuration configuration;

	public static final ConfigurationKey<String> optimizationCacheLocationKey = ConfigurationKey
			.Create(String.class, "optimizationCache.location",
					"directory containing the cached results of optimizations",
					"~/.roofline/optimizationCache");

	public static class OptimizationParameters {
		public KernelDescriptionBase kernel;
		public ParameterSpace optimizationSpace;
		public Coordinate measurementPoint;
		public Comparison comparison;

		public OptimizationParameters(KernelDescriptionBase kernel,
				ParameterSpace optimizationSpace, Coordinate measurementPoint,
				Comparison comparison) {
			this.kernel = kernel;
			this.optimizationSpace = optimizationSpace;
			this.measurementPoint = measurementPoint;
			this.comparison = comparison;
		}
	}

	public String getHash(KernelDescriptionBase kernel,
			ParameterSpace optimizationSpace, Coordinate measurementPoint,
			Comparison comparison) {
		return getHash(new OptimizationParameters(kernel,
				optimizationSpace, measurementPoint, comparison));
	}

	private String getHash(OptimizationParameters parameterObject) {
		return hashService.hashObject(parameterObject);
	}

	public Coordinate getOptimum(String hash) {
		log.debug("getOptimum of " + hash);
		return (Coordinate) cacheService.getCachedValue(hash,
				configuration.get(optimizationCacheLocationKey));
	}

	public void storeOptimum(Coordinate optimum, String hash) throws Error {
		log.debug("storeOptimum of " + hash);
		cacheService.store(optimum, hash,
				configuration.get(optimizationCacheLocationKey));
	}
}
