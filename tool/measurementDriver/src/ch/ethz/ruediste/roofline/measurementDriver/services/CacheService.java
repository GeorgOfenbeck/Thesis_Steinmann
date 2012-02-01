package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.io.*;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

public class CacheService {
	@Inject
	public XStream xStream;

	/**
	 * return the cache file for the given hash and cacheLocation
	 */
	public File getCacheFile(String hash, String cacheLocationString) {
		// replace a starting tilde with the user home directory
		if (cacheLocationString.startsWith("~")) {
			cacheLocationString = System.getProperty("user.home")
					+ cacheLocationString.substring(1);
		}

		File cacheLocation = new File(cacheLocationString);

		// get the file which should contain the cached measurement
		File cacheFile = new File(cacheLocation, hash);
		return cacheFile;
	}

	/**
	 * store a value under the given hash
	 */
	public void store(Object value, String hash, String location) throws Error {
		try {
			// get the cache file
			File cacheFile = getCacheFile(hash, location);

			// open the cache file for writing
			cacheFile.getParentFile().mkdirs();
			FileOutputStream output = new FileOutputStream(cacheFile, false);

			// serialize the measurement result to the cache file
			xStream.toXML(value, output);

			// finish writing
			output.close();

		}
		catch (IOException e) {
			throw new Error("Error while storing measurement result in cache",
					e);
		}
	}

	/**
	 * get the value cached under the hash
	 */
	public Object getCachedValue(String hash, String location) {

		// get the cache file
		File cacheFile = getCacheFile(hash, location);

		// check if the value in the cache
		if (cacheFile.exists()) {
			// if a cached value is present, deserialize it and return
			return xStream.fromXML(cacheFile);
		}

		// if no cached value is found, return null;
		return null;
	}

	/**
	 * delete the value stored under the hash
	 */
	public void delete(String hash, String location) {
		// get the cache file
		File cacheFile = getCacheFile(hash, location);

		// delete it
		cacheFile.delete();
	}

}
