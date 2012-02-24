package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import java.io.File;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.entities.MeasurementResult;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

public class MeasurementResultRepository {
	private static Logger log = Logger
			.getLogger(MeasurementResultRepository.class);
	public static final ConfigurationKey<String> cacheLocationKey = ConfigurationKey
			.Create(String.class, "cache.location",
					"directory containing the cached results of measurements",
					"~/.roofline/cache");

	@Inject
	public MeasurementService measurementService;

	@Inject
	public XStream xStream;

	@Inject
	public Configuration configuration;

	@Inject
	public HashService hashService;

	@Inject
	public RuntimeMonitor runtimeMonitor;

	@Inject
	public CacheService cacheService;

	/**
	 * load the measurement result form the cache. return null if no cache entry
	 * was found
	 */
	public MeasurementResult getMeasurementResult(
			MeasurementHash measurementHash) {
		log.debug("loading result from cache");
		runtimeMonitor.loadMeasurementResultsCategory.enter();
		try {
			return (MeasurementResult) cacheService.getCachedValue(
					measurementHash.getValue(),
					configuration.get(cacheLocationKey));
		}
		finally {
			runtimeMonitor.loadMeasurementResultsCategory.leave();
		}
	}

	/**
	 * get the cache file for the hash of a measurement (as generated with
	 * HashService.getMeasurementHash())
	 */
	public File getCacheFile(MeasurementHash hash) {
		return cacheService.getCacheFile(hash.getValue(),
				configuration.get(cacheLocationKey));
	}

	/** store a measurement result in the cache */
	public void store(MeasurementResult result, MeasurementHash hash) {
		cacheService.store(result, hash.getValue(),
				configuration.get(cacheLocationKey));
	}

	/** delete the cached data for a measurement */
	public void delete(MeasurementHash hash) {

		cacheService.delete(hash.getValue(),
				configuration.get(cacheLocationKey));
	}

}
