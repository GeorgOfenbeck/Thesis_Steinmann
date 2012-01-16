package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import java.io.*;
import java.security.*;
import java.util.HashSet;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

public class MeasurementRepository {
	public static final ConfigurationKey<String> cacheLocationKey = ConfigurationKey
			.Create(String.class, "cache.location",
					"directory containing the cached results of measurements",
					"~/.roofline/cache");

	public static final ConfigurationKey<String> messageDigestKey = ConfigurationKey
			.Create(String.class,
					"cache.messageDigest",
					"Algorithm used to build the hash keys in the measurement cache",
					"MD5");

	public final static ConfigurationKey<Boolean> useCachedResultsKey = ConfigurationKey
			.Create(Boolean.class, "useCachedResults",
					"indicates if the cached results should be used", true);

	private final HashSet<String> newMeasurements = new HashSet<String>();

	/**
	 * Output stream discarding everything
	 */
	private class NullOutputStream extends OutputStream {

		@Override
		public void write(int arg0) throws IOException {
			// discard everything
		}

	}

	@Inject
	public MeasurementService measurementService;

	@Inject
	public XStream xStream;

	@Inject
	public Configuration configuration;

	/**
	 * load the measurement result form the cache. return null if no cache entry
	 * was found
	 */
	public MeasurementResult getMeasurementResult(
			MeasurementDescription measurement) {
		// calculate the key
		String key = getCacheKey(measurement);

		if (
		// should we use cached results?
		configuration.get(useCachedResultsKey)
		// or was the measurement measured already in this session?
				|| newMeasurements.contains(key)) {
			// try to load the measurement results from the cache

			// get the cache file
			File cacheFile = getCacheFile(key);

			// check if the value is already cached
			if (cacheFile.exists()) {
				// if a cached value is present, deserialize it and return
				return (MeasurementResult) xStream.fromXML(cacheFile);
			}
		}

		// if no cached value is found, or if it should not be used
		// (configuration), return null;
		return null;
	}

	/** get the cache file for a measurement */
	private File getCacheFile(MeasurementDescription measurement) {
		// calculate the key
		String key = getCacheKey(measurement);

		// get the cache file
		File cacheFile = getCacheFile(key);
		return cacheFile;
	}

	/**
	 * get the cache file for the key of a measurement (as generated with
	 * getCacheKey)
	 */
	private File getCacheFile(String key) {
		// retrieve the cache location directory
		String cacheLocationString = configuration.get(cacheLocationKey);

		// replace a starting tilde with the user home directory
		if (cacheLocationString.startsWith("~")) {
			cacheLocationString = System.getProperty("user.home")
					+ cacheLocationString.substring(1);
		}

		File cacheLocation = new File(cacheLocationString);

		// get the file which should contain the cached measurement
		File cacheFile = new File(cacheLocation, key);
		return cacheFile;
	}

	/** store a measurement result in the cache */
	public void store(MeasurementResult result) {
		try {
			// calculate the key
			String key = getCacheKey(result.getMeasurement());

			// get the cache file
			File cacheFile = getCacheFile(key);

			// open the cache file for writing
			cacheFile.getParentFile().mkdirs();
			FileOutputStream output = new FileOutputStream(cacheFile, false);

			// serialize the measurement result to the cache file
			xStream.toXML(result, output);

			// finish writing
			output.close();

			// mark the measurement as have been measured within this session
			newMeasurements.add(key);

		} catch (IOException e) {
			throw new Error("Error while storing measurement result in cache",
					e);
		}
	}

	/** delete the cached data for a measurement */
	public void delete(MeasurementDescription measurement) {
		// get the cache file
		File cacheFile = getCacheFile(measurement);

		// delete it
		cacheFile.delete();
	}

	/**
	 * get a key for a measurement, to be used with the other methods of this
	 * service
	 */
	private String getCacheKey(MeasurementDescription measurement) {
		// get the message digest
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(configuration.get(messageDigestKey));
		} catch (NoSuchAlgorithmException e) {
			throw new Error(
					String.format(
							"Message digest algorithm %s not found. Set by %s. Needed for cache key generation",
							configuration.get(messageDigestKey),
							messageDigestKey.getKey()), e);
		}

		// setup a digest output stream, which digests a message while
		// forwarding to the
		// embedded stream
		DigestOutputStream digestOutputStream = new DigestOutputStream(
				new NullOutputStream(), md);

		// digest the serialized xml
		xStream.toXML(measurement, digestOutputStream);

		// get the key
		byte[] mdbytes = md.digest();

		// convert the byte to hex format
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			String hex = Integer.toHexString(0xff & mdbytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}

		return hexString.toString();
	}

}
