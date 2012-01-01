package ch.ethz.ruediste.roofline.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import ch.ethz.ruediste.roofline.dom.MultiLanguageSerializationService;
import ch.ethz.ruediste.roofline.dom.MultiLanguageTestClass;

//import com.sun.xml.internal.ws.util.ByteArrayBuffer;

public class SerializationTest {

	MultiLanguageTestClass testObject;
	ByteArrayOutputStream buffer;
	MultiLanguageSerializationService serializationService = new MultiLanguageSerializationService();

	@Before
	public void setup() {
		buffer = new ByteArrayOutputStream();

		testObject = new MultiLanguageTestClass();
		testObject.setLongField(2);
		testObject.setBoolField(true);
		testObject.setIntField(3);
		testObject.setDoubleField(2.4);
		testObject.setStringField("Hello World");
		testObject.setUlongField(new BigInteger("1234"));

		testObject.getPrimitiveList().add(2);
		testObject.getPrimitiveList().add(3);
		testObject.getPrimitiveList().add(4);

		MultiLanguageTestClass testObject2 = new MultiLanguageTestClass();
		testObject.setReferenceField(testObject2);

		testObject2.getReferenceList().add(new MultiLanguageTestClass());
		testObject2.getReferenceList().add(new MultiLanguageTestClass());
	}

	private void checkTestObject(MultiLanguageTestClass deserializedTestObject) {
		assertEquals(2, deserializedTestObject.getLongField());
		assertEquals(true, deserializedTestObject.getBoolField());
		assertEquals(3, deserializedTestObject.getIntField());
		assertTrue(2.4 == deserializedTestObject.getDoubleField());
		assertEquals("Hello World", deserializedTestObject.getStringField());
		assertTrue(new BigInteger("1234").equals(deserializedTestObject
				.getUlongField()));

		assertEquals(3, deserializedTestObject.getPrimitiveList().size());
		assertEquals(2, (int) deserializedTestObject.getPrimitiveList().get(0));
		assertEquals(3, (int) deserializedTestObject.getPrimitiveList().get(1));
		assertEquals(4, (int) deserializedTestObject.getPrimitiveList().get(2));

		MultiLanguageTestClass testObject2 = deserializedTestObject
				.getReferenceField();
		assertEquals(2, testObject2.getReferenceList().size());
	}

	/**
	 * Test java serialization and deserialization
	 */
	@Test
	public void testJava() {

		ByteArrayOutputStream dummyBuffer = new ByteArrayOutputStream();
		serializationService.Serialize(testObject, System.out);

		dummyBuffer.reset();
		serializationService.Serialize(testObject, buffer);

		MultiLanguageTestClass deserializedTestObject = (MultiLanguageTestClass) serializationService
				.DeSerialize(new ByteArrayInputStream(buffer.toByteArray()));

		checkTestObject(deserializedTestObject);

	}

	/**
	 * Test both Java and C serialization and deserialization
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testC() throws IOException, InterruptedException {
		// setup input file
		File inputFile = new File(
				"../measuringCore/build/serializationTestInput");
		File outputFile = new File(
				"../measuringCore/build/serializationTestOutput");

		// serialize test object
		FileOutputStream inputFileStream = new FileOutputStream(inputFile);
		serializationService
				.Serialize(testObject, inputFileStream);
		inputFileStream.close();

		// run c code
		System.out.println(new File(".").getAbsolutePath());
		File workDir = new File("../measuringCore/build");
		Process p = Runtime.getRuntime().exec(
				new String[] { "bash", "-c",
						"./measuringCore serializationTest" }, null, workDir);
		p.waitFor();

		// copy output of the make process to the output of this process
		byte[] buf = new byte[100];
		InputStream input = p.getInputStream();
		int len = 0;
		while ((len = input.read(buf)) > 0) {
			System.out.write(buf, 0, len);
		}

		// deserialize test object
		FileInputStream outputFileStream = new FileInputStream(outputFile);

		MultiLanguageTestClass deserializedTestObject = (MultiLanguageTestClass) serializationService
				.DeSerialize(outputFileStream);
		outputFileStream.close();

		// check serialization
		checkTestObject(deserializedTestObject);
	}
}
