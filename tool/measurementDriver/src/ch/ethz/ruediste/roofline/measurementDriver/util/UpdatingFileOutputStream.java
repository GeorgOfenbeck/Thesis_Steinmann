package ch.ethz.ruediste.roofline.measurementDriver.util;

import java.io.*;

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

	public UpdatingFileOutputStream(File file) throws FileNotFoundException {
		this.file = file;

		if (file.exists()) {
			raf = new RandomAccessFile(file, "r");
		}
		else {
			raf = new RandomAccessFile(file, "rw");
			this.writing = true;
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (isWriting()) {
			// if output is set already, we found a difference
			// previously. So directly forward b to the output
			raf.write(b);
		}
		else {
			// read the next character
			int ch = raf.read();

			// check if the characters match
			if (b != ch) {
				// they don't match
				switchToOutput(ch != -1);

				// write character
				raf.write(b);
			}

		}
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void switchToOutput(boolean decreasePosition)
			throws FileNotFoundException, IOException {
		this.writing = true;

		// get current position
		long position = raf.getFilePointer();

		// since a character has been already read, decrease the position by one
		if (decreasePosition && position > 0) {
			position--;
		}

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
		// when writing, truncate the file
		if (isWriting()) {
			raf.setLength(raf.getFilePointer());
		}
		else {
			// check that we are at the end of the file
			long position = raf.getFilePointer();
			if (raf.length() != position) {
				// truncate the file
				switchToOutput(false);
			}
		}

		// close the file
		raf.close();
	}

	public boolean isWriting() {
		return writing;
	}
}
