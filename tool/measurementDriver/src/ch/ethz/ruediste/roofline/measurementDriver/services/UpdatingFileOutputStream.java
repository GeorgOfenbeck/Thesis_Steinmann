package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * A file output stream which only writes to the file when changes are detected.
 * 
 */
public class UpdatingFileOutputStream extends OutputStream {
	private final File file;
	/**
	 * initially opened in read only mode. when the first difference is found,
	 * it is switched to read/write mode
	 */
	private RandomAccessFile raf;

	/**
	 * set to true when the first difference is found
	 */
	private boolean writing;

	UpdatingFileOutputStream(File file) throws FileNotFoundException {
		this.file = file;
		raf = new RandomAccessFile(file, "r");
	}

	@Override
	public void write(int b) throws IOException {
		if (writing) {
			// if output is set already, we found a difference
			// previously. So directly forward b to the output
			raf.write(b);
		}
		else
		{
			// read the next character
			int ch = raf.read();

			// check if the characters match
			if (b != ch) {
				// they don't match
				switchToOutput();

				// write character
				raf.write(b);
			}

		}
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void switchToOutput() throws FileNotFoundException, IOException {
		writing = true;

		// get current position
		long position = raf.getFilePointer();

		// close input
		raf.close();

		// open file in rw mode
		raf = new RandomAccessFile(file, "rw");

		// seek output file
		raf.seek(position);

		// truncate file
		raf.setLength(position);
	}

	@Override
	public void close() throws IOException {
		raf.close();
	}
}
