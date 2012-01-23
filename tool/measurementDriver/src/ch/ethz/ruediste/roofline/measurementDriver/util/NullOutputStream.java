package ch.ethz.ruediste.roofline.measurementDriver.util;

import java.io.*;

/**
 * Output stream discarding everything
 */
public class NullOutputStream extends OutputStream {

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