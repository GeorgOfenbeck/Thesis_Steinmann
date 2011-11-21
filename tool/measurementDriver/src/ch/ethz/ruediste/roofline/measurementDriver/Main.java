package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import ch.ethz.ruediste.roofline.sharedDOM.MeasurementCollection;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementDescription;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementResult;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementResultCollection;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurerOutputCollection;
import ch.ethz.ruediste.roofline.sharedDOM.MultiLanguageSerializationService;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Main {

	public static void main(String args[]) {
		System.out.println("Performing Measurements");
		XStream xStream = new XStream(new DomDriver());
		MultiLanguageSerializationService serializationService = new MultiLanguageSerializationService();

		File measuringCoreDir = new File("../measuringCore/Debug");

		// load measurement collection
		System.out.println("Loading measurement descriptions");
		MeasurementCollection coll = (MeasurementCollection) xStream
				.fromXML(new File("measurement.xml"));

		// instantiate measurement result collection
		MeasurementResultCollection results = new MeasurementResultCollection();

		// iterate over all measurements
		for (MeasurementDescription measurement : coll.getDescriptions()) {
			System.out.println("processing the following measurement:");
			xStream.toXML(measurement, System.out);
			System.out.println();

			// create result
			MeasurementResult result = new MeasurementResult();
			results.add(result);
			result.setMeasurement(measurement);

			try {
				// write configuration
				File configFile = new File(measuringCoreDir, "config");
				File configDefFile = new File(measuringCoreDir,
						"../src/configDef.h");
				FileOutputStream config = new FileOutputStream(configFile);
				FileOutputStream configDef = new FileOutputStream(configDefFile);
				serializationService.Serialize(measurement, config, configDef);
				config.close();

				// create optimization files
				System.out.println("creating optimization file");
				File optimizationFile = new File(measuringCoreDir,
						"../makefile.init");
				PrintStream optimizationPrintStream = new PrintStream(
						optimizationFile);
				optimizationPrintStream.printf("OPTIMIZATION = %s\n",
						measurement.getOptimization());
				optimizationPrintStream.close();

				// build
				System.out.println("building measuring core");
				runCommand(measuringCoreDir, new String[] { "make", "clean" });
				runCommand(measuringCoreDir, new String[] { "make", "all" });

				// remove output file
				System.out.println("removing output file");
				File outputFile = new File(measuringCoreDir, "output");
				outputFile.delete();

				// run measurement
				System.out.println("running measurement");
				runCommand(measuringCoreDir, new String[] { "./measuringCore" });

				// parse measurer output
				System.out.println("parsing measurement output");
				FileInputStream output = new FileInputStream(outputFile);
				MeasurerOutputCollection outputs = (MeasurerOutputCollection) serializationService
						.DeSerialize(output);

				// add output to result
				result.add(outputs);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("writing results");
		try {
			xStream.toXML(results, new FileOutputStream(
					"measurementResults.xml"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xStream.toXML(results, System.out);
	}

	private static void createDefFiles(MeasurementDescription measurement) {

	}

	private static void runCommand(File workingDirectory, String[] command) {
		try {
			// running make
			Process p = Runtime.getRuntime().exec(
					command, null, workingDirectory);
			p.waitFor();

			// copy output of the make process to the output of this process
			System.out.println(">>>>");
			byte[] buf = new byte[100];
			InputStream input = p.getInputStream();
			int len;
			while ((len = input.read(buf)) > 0) {
				System.out.write(buf, 0, len);
			}
			input = p.getErrorStream();
			while ((len = input.read(buf)) > 0) {
				System.out.write(buf, 0, len);
			}
			System.out.println("<<<<");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
