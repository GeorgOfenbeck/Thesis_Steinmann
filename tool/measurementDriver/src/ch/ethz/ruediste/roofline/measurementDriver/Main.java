package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import ch.ethz.ruediste.roofline.sharedDOM.MeasurementCollection;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementDescription;
import ch.ethz.ruediste.roofline.sharedDOM.MultiLanguageSerializationService;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Main {

	public static void main(String args[]){
		System.out.println("Performing Measurements");
		XStream xStream=new XStream(new DomDriver());
		MultiLanguageSerializationService serializationService
			=new MultiLanguageSerializationService();
		
		// load measurement collection
		System.out.println("Loading measurement descriptions");
		MeasurementCollection coll=(MeasurementCollection) xStream.fromXML(new File("measurement.xml"));
		
		for (MeasurementDescription measurement: coll.getDescriptions()){
			System.out.println("processing the following measurement:");
			xStream.toXML(measurement, System.out);
			System.out.println();
			
			// write configuration
			try {
				File configFile=new File("config");
				FileOutputStream conf = new FileOutputStream(configFile);
				serializationService.Serialize(measurement, conf);
				conf.close();
				
				FileInputStream confIn=new FileInputStream(configFile);
				MeasurementDescription measurement2
					=(MeasurementDescription) serializationService.DeSerialize(confIn);
				confIn.close();
				
				System.out.println("Serialized/Deserialized:");
				xStream.toXML(measurement2);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// create def files
			System.out.println("creating def files");
			
			// build
			System.out.println("building measuring core");
			try {
				// running make
				File workDir=new File("../measuringCore/Debug");
				Process p = Runtime.getRuntime().exec(new String[]{"make","all"},null,workDir);
				p.waitFor();
				
				// copy output of the make process to the output of this process
				byte[] buf=new byte[100];
				InputStream input=p.getInputStream();
				int len=0;
				while ((len=input.read(buf))>0){
					System.out.write(buf,0,len);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// run measurement
			System.out.println("running measurement");
			
			// parse measurer output
			System.out.println("parsing measurement output");
			
			// add output to result
		}
		
		System.out.println("writing result");
	}
}