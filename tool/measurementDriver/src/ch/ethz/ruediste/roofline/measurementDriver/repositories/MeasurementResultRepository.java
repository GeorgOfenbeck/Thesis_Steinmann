package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import java.io.*;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;

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

	/**
	 * load the measurement result form the cache. return null if no cache entry
	 * was found
	 */
	public MeasurementResult getMeasurementResult(
			MeasurementHash measurementHash) {
		log.debug("loading result from cache");
		try {
			runtimeMonitor.loadMeasurementResultsCategory.enter();
			// get the cache file
			File cacheFile = getCacheFile(measurementHash);

			// check if the value in the cache
			if (cacheFile.exists()) {
				// if a cached value is present, deserialize it and return
				return (MeasurementResult) xStream.fromXML(cacheFile);
			}

			// if no cached value is found, return null;
			return null;
		} finally {
			runtimeMonitor.loadMeasurementResultsCategory.leave();
		}
	}

	/**
	 * get the cache file for the hash of a measurement (as generated with
	 * HashService.getMeasurementHash())
	 */
	private File getCacheFile(MeasurementHash hash) {
		// retrieve the cache location directory
		String cacheLocationString = configuration.get(cacheLocationKey);

		// replace a starting tilde with the user home directory
		if (cacheLocationString.startsWith("~")) {
			cacheLocationString = System.getProperty("user.home")
					+ cacheLocationString.substring(1);
		}

		File cacheLocation = new File(cacheLocationString);

		// get the file which should contain the cached measurement
		File cacheFile = new File(cacheLocation, hash.getValue());
		return cacheFile;
	}

	/** store a measurement result in the cache */
	public void store(MeasurementResult result, MeasurementHash hash) {
		try {
			// get the cache file
			File cacheFile = getCacheFile(hash);

			// open the cache file for writing
			cacheFile.getParentFile().mkdirs();
			FileOutputStream output = new FileOutputStream(cacheFile, false);

			// serialize the measurement result to the cache file
			xStream.toXML(result, output);

			// finish writing
			output.close();

		} catch (IOException e) {
			throw new Error("Error while storing measurement result in cache",
					e);
		}
	}

	/** delete the cached data for a measurement */
	public void delete(MeasurementHash hash) {

		// get the cache file
		File cacheFile = getCacheFile(hash);

		// delete it
		cacheFile.delete();
	}

}
