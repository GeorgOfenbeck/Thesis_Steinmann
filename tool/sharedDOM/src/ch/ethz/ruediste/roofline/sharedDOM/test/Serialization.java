package ch.ethz.ruediste.roofline.sharedDOM.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;

import ch.ethz.ruediste.roofline.sharedDOM.MeasurementDescription;
import ch.ethz.ruediste.roofline.sharedDOM.MemoryLoadKernelDescription;
import ch.ethz.ruediste.roofline.sharedDOM.MultiLanguageSerializationService;

public class Serialization {

	@Test
	public void test() {
		MultiLanguageSerializationService serializationService 
			=new MultiLanguageSerializationService();
		
		ByteArrayBuffer buffer=new ByteArrayBuffer();
		
		MemoryLoadKernelDescription desc =new MemoryLoadKernelDescription();
		desc.setBlockSize(2);
		
		serializationService.Serialize(desc, System.out);
		
		serializationService.Serialize(desc, buffer);
				
		MemoryLoadKernelDescription desc2
			=(MemoryLoadKernelDescription) serializationService.DeSerialize(buffer.newInputStream());
		
		assertEquals(2,desc2.getBlockSize());
	}

}
