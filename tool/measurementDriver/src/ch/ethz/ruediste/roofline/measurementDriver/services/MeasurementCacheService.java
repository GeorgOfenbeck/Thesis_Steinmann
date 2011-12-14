package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.ConfigurationKey;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

public class MeasurementCacheService {
	public static final ConfigurationKey<String> cacheLocationKey = ConfigurationKey
			.Create(
					String.class,
					"cache.location",
					"directory containing the cached results of measurements",
					"~/.roofline/cache");

	public static final ConfigurationKey<String> messageDigestKey = ConfigurationKey
			.Create(
					String.class,
					"cache.messageDigest",
					"Algorithm used to build the hash keys in the measurement cache",
					"MD5");

	private class NullOutputStream extends OutputStream {

		@Override
		public void write(int arg0) throws IOException {
			// discard everything
		}

	}

	@Inject
	public XStream xStream;

	@Inject
	public Configuration configuration;

	/**
	 * load the measurement result form the cache. return null if no cache entry
	 * was found
	 */
	public MeasurementResult loadFromCache(MeasurementDescription measurement) {
		// get the cache file
		File cacheFile = getCacheFile(measurement);

		// check if the value is already cached
		if (cacheFile.exists()) {
			// if a cached value is present, deserialize it and return
			return (MeasurementResult) xStream.fromXML(cacheFile);
		}

		// if no cached value is found, return null;
		return null;
	}

	/** get the cache file for a measurement */
	public File getCacheFile(MeasurementDescription measurement) {
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
	public void storeInCache(MeasurementResult result) {
		try {
			// get the cache file
			File cacheFile = getCacheFile(result.getMeasurement());

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
	public void deleteFromCache(MeasurementDescription measurement) {
		// get the cache file
		File cacheFile = getCacheFile(measurement);

		// delete it
		cacheFile.delete();
	}

	/**
	 * get a key for a measurement, to be used with the other methods of this
	 * service
	 */
	public String getCacheKey(MeasurementDescription measurement) {
		// get the message digest
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(configuration
					.get(messageDigestKey));
		} catch (NoSuchAlgorithmException e) {
			throw new Error(
					String.format(
							"Message digest algorithm %s not found. Set by %s. Needed for cache key generation",
							configuration.get(messageDigestKey),
							messageDigestKey),
					e);
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
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}

		return hexString.toString();
	}
}
