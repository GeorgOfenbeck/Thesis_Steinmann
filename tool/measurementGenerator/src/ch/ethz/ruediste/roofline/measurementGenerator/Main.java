package ch.ethz.ruediste.roofline.measurementGenerator;

import java.io.FileWriter;
import java.io.IOException;

import ch.ethz.ruediste.roofline.sharedDOM.ExecutionTimeMeasurerDescription;
import ch.ethz.ruediste.roofline.sharedDOM.KBestMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementCollection;
import ch.ethz.ruediste.roofline.sharedDOM.MemoryLoadKernelDescription;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Main {

	public static void main(String args[]) {
		System.out.println("Generating Measurements");

		MeasurementCollection coll = new MeasurementCollection();

		// create measurements
		MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
		kernel.setBlockSize(1<<25);

		coll.addDescription(
				kernel,
				new KBestMeasurementSchemeDescription(),
				new ExecutionTimeMeasurerDescription(),
				10
				);

		// store measurement description
		XStream xStream = new XStream(new DomDriver());
		try {
			FileWriter writer = new FileWriter("measurement.xml");
			xStream.toXML(coll, writer);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xStream.toXML(coll, System.out);
	}
}
