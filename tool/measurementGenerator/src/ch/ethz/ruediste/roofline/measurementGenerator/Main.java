package ch.ethz.ruediste.roofline.measurementGenerator;

import java.io.FileWriter;
import java.io.IOException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import ch.ethz.ruediste.roofline.sharedDOM.MeasurementCollection;
import ch.ethz.ruediste.roofline.sharedDOM.MemoryLoadKernelDescription;

public class Main {

	public static void main(String args[]){
		System.out.println("Generating Measurements");
		
		MeasurementCollection coll=new MeasurementCollection();
		
		// create measurements
		coll.addDescription(new MemoryLoadKernelDescription());
		
		// store measurement description
		XStream xStream=new XStream(new DomDriver());
		try {
			xStream.toXML(coll, new FileWriter("measurement.xml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
