package ch.ethz.ruediste.roofline.measurementDriver.dom.repositories;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.OptimizationParameters.Comparison;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.services.*;
import ch.ethz.ruediste.roofline.sharedEntities.KernelBase;

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

	public String getHash(KernelBase kernel, ParameterSpace optimizationSpace,
			Coordinate measurementPoint, Comparison comparison) {
		return getHash(new OptimizationParameters(kernel, optimizationSpace,
				measurementPoint, comparison));
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
