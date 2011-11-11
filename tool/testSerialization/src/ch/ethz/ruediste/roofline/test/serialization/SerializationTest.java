package ch.ethz.ruediste.roofline.test.serialization;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import ch.ethz.ruediste.roofline.sharedDOM.MultiLanguageSerializationService;
import ch.ethz.ruediste.roofline.sharedDOM.MultiLanguageTestClass;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;

public class SerializationTest {

	MultiLanguageTestClass testObject;
	ByteArrayBuffer buffer;
	MultiLanguageSerializationService serializationService 
		=new MultiLanguageSerializationService();
	
	@Before
	public void setup(){
		buffer=new ByteArrayBuffer();
		
		testObject =new MultiLanguageTestClass();
		testObject.setLongField(2);
		testObject.setBoolField(true);
		testObject.setIntField(3);
	}
	
	
	private void checkTestObject(MultiLanguageTestClass deserializedTestObject) {
		assertEquals(2,deserializedTestObject.getLongField());
		assertEquals(true,deserializedTestObject.getBoolField());
		assertEquals(3,deserializedTestObject.getIntField());
	}
	
	/** Test java serialization and deserialization
	 */
	@Test
	public void testJava() {
		serializationService.Serialize(testObject, System.out);
		
		serializationService.Serialize(testObject, buffer);
				
		MultiLanguageTestClass deserializedTestObject
			=(MultiLanguageTestClass) serializationService.DeSerialize(buffer.newInputStream());
		
		checkTestObject(deserializedTestObject);
		
	}


	/**
	 * Test both Java and C serialization and deserialization
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testC() throws IOException, InterruptedException{
			// setup input file
			File inputFile=new File("../measuringCore/Debug/serializationTestInput");
			File outputFile=new File("../measuringCore/Debug/serializationTestOutput");
			
			// serialize test object
			FileOutputStream inputFileStream=new FileOutputStream(inputFile);
			serializationService.Serialize(testObject, inputFileStream);
			inputFileStream.close();
			
			// run c code
			System.out.println(new File(".").getAbsolutePath());
			File workDir=new File("../measuringCore/Debug");
			Process p = Runtime.getRuntime().exec(new String[]{"bash","-c", "./measuringCore serializationTest"},null,workDir);
			p.waitFor();
			
			// copy output of the make process to the output of this process
			byte[] buf=new byte[100];
			InputStream input=p.getInputStream();
			int len=0;
			while ((len=input.read(buf))>0){
				System.out.write(buf,0,len);
			}
			
			// deserialize test object
			FileInputStream outputFileStream=new FileInputStream(outputFile);
			
			MultiLanguageTestClass deserializedTestObject
				=(MultiLanguageTestClass) serializationService.DeSerialize(outputFileStream);
			outputFileStream.close();
		
			// check serialization
			checkTestObject(deserializedTestObject);
	}
}
