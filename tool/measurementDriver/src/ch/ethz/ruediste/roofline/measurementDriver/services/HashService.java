package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.io.*;
import java.security.*;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

public class HashService {
	private static Logger log = Logger.getLogger(HashService.class);

	public static final ConfigurationKey<String> messageDigestKey = ConfigurationKey
			.Create(String.class,
					"cache.messageDigest",
					"Algorithm used to build the hash keys in the measurement cache",
					"MD5");

	@Inject
	public Configuration configuration;

	@Inject
	public XStream xStream;

	@Inject
	public MeasuringCoreLocationService measuringCoreLocationService;

	@Inject
	RuntimeMonitor runtimeMonitor;

	/**
	 * Output stream discarding everything
	 */
	private class NullOutputStream extends OutputStream {

		@Override
		public void write(int arg0) throws IOException {
			// discard everything
		}

		@Override
		public void write(byte[] b) throws IOException {
			// discard everything
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			// discard everything
		}
	}

	/**
	 * hash a file
	 * 
	 * @throws IOException
	 */
	public String hashFile(File file) throws IOException {
		log.debug("hashing file " + file.getAbsolutePath());
		runtimeMonitor.hashingCategory.enter();

		DigestOutputStream digestOutputStream = openDigestOutputStream(new NullOutputStream());

		FileInputStream input = new FileInputStream(file);
		byte[] buffer = new byte[512];
		int len;
		while ((len = input.read(buffer)) > 0) {
			digestOutputStream.write(buffer, 0, len);
		}
		String hash = getHash(digestOutputStream);
		runtimeMonitor.hashingCategory.leave();
		return hash;
	}

	/**
	 * hash an object
	 */
	public String hashObject(Object obj) {
		runtimeMonitor.hashingCategory.enter();
		DigestOutputStream digestOutputStream = openDigestOutputStream(new NullOutputStream());

		// digest the serialized xml
		xStream.toXML(obj, digestOutputStream);

		String hash = getHash(digestOutputStream);
		runtimeMonitor.hashingCategory.leave();
		return hash;
	}

	private String getHash(DigestOutputStream digestOutputStream) {
		// get the key
		byte[] mdbytes = digestOutputStream.getMessageDigest().digest();

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

	private DigestOutputStream openDigestOutputStream(
			NullOutputStream outputStream) throws Error {
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
				outputStream, md);
		return digestOutputStream;
	}

	/**
	 * hash a measurement. This should be an unique identifier for the
	 * measurement
	 */
	public MeasurementHash getMeasurementHash(MeasurementDescription measurement) {
		return new MeasurementHash(hashObject(measurement));
	}

	/**
	 * hash a measurement. This should be an unique identifier for the
	 * measurement
	 */
	public CoreHash getMeasuringCoreHash() throws IOException {
		return new CoreHash(
				hashFile(measuringCoreLocationService
						.getMeasuringCoreExecutable()));
	}
}
