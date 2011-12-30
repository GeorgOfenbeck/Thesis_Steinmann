package ch.ethz.ruediste.roofline.test;

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Test;

import ch.ethz.ruediste.roofline.measurementDriver.UpdatingFileOutputStream;

public class UpdatingFileOutputStreamTest {
	@Test
	public void testIdentical() throws FileNotFoundException,
			InterruptedException {
		File file = new File("updatingFileOutputStream.test");

		// initialize file
		{
			PrintStream out = new PrintStream(file);
			out.println("Hello");
			out.close();
		}

		// remember modification time
		long modTime = file.lastModified();

		// seleep 1.5s to make modification time different
		Thread.sleep(1500);

		// overwrite file with same content
		{
			PrintStream out = new PrintStream(
					new UpdatingFileOutputStream(file));
			out.println("Hello");
			out.close();
		}

		// check that the file was not modified
		assertEquals(modTime, file.lastModified());

	}

	@Test
	public void testUpdate() throws InterruptedException, IOException {
		// equal length
		updateTestHelper("Hello", "Hellu");
		updateTestHelper("Hello", "Hallo");

		// shorter
		updateTestHelper("Hello", "Hell");
		updateTestHelper("Hello", "Helo");

		// longer
		updateTestHelper("Hello", "Helloo");
		updateTestHelper("Hello", "Heello");

		// toEmpty
		updateTestHelper("Hello", "");

		// empty to something
		updateTestHelper("", "Hello");
	}

	/**
	 * @param initialContent
	 * @param updatedContent
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void updateTestHelper(String initialContent, String updatedContent)
			throws FileNotFoundException, IOException {
		File file = new File("updatingFileOutputStream.test");

		// initialize file
		{
			PrintStream out = new PrintStream(file);

			out.print(initialContent);
			out.close();
		}

		// overwrite file with different content
		{
			PrintStream out = new PrintStream(
					new UpdatingFileOutputStream(file));

			out.print(updatedContent);
			out.close();
		}

		// check that the file was modified
		{
			FileInputStream in = new FileInputStream(file);

			int i = 0;
			int ch = 0;
			while ((ch = in.read()) != -1 && i < updatedContent.length()) {
				String message = String.format(
						"Seen <%s>, expected %c to come, but found %c; ",
						updatedContent.substring(0, i),
						updatedContent.codePointAt(i), ch);
				assertEquals(message, updatedContent.codePointAt(i), ch);
				System.out.write(ch);
				i++;
			}
			in.close();
			assertEquals(-1, ch);
			assertEquals(updatedContent.length(), i);
		}
	}
}
