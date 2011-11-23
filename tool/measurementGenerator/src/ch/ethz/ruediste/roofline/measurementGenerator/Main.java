package ch.ethz.ruediste.roofline.measurementGenerator;

import java.io.FileWriter;
import java.io.IOException;

import ch.ethz.ruediste.roofline.sharedDOM.ExecutionTimeMeasurerDescription;
import ch.ethz.ruediste.roofline.sharedDOM.KBestMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementCollection;
import ch.ethz.ruediste.roofline.sharedDOM.MeasurementDescription;
import ch.ethz.ruediste.roofline.sharedDOM.MemoryLoadKernelDescription;
import ch.ethz.ruediste.roofline.sharedDOM.PerfEventMeasurerDescription;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Main {

	public static void main(String args[]) {
		System.out.println("Generating Measurements");

		MeasurementCollection coll = new MeasurementCollection();

		// create measurements

		// addCycleTimer(coll);

		MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
		kernel.setBlockSize(50);

		PerfEventMeasurerDescription measurerPerfEvents = new PerfEventMeasurerDescription();
		measurerPerfEvents.getEvents().add(
				"perf::PERF_COUNT_SW_CONTEXT_SWITCHES:u");

		MeasurementDescription desc;
		desc = new MeasurementDescription();
		desc.setKernel(kernel);
		desc.setScheme(new KBestMeasurementSchemeDescription());
		desc.setMeasurer(measurerPerfEvents);
		desc.setNumberOfMeasurements(3);
		desc.setOptimization("-O3");
		coll.addDescription(desc);

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

	private static void addCycleTimer(MeasurementCollection coll) {
		PerfEventMeasurerDescription measurerPerfEvents = new PerfEventMeasurerDescription();
		measurerPerfEvents.getEvents().add("core::UNHALTED_REFERENCE_CYCLES:u");

		ExecutionTimeMeasurerDescription measurerExecutionTime = new ExecutionTimeMeasurerDescription();

		KBestMeasurementSchemeDescription scheme = new KBestMeasurementSchemeDescription();

		for (long blockSize = 1; blockSize < 1 << 29; blockSize = blockSize << 1) {
			MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
			kernel.setBlockSize(blockSize);

			MeasurementDescription desc;

			desc = new MeasurementDescription();
			desc.setKernel(kernel);
			desc.setScheme(scheme);
			desc.setMeasurer(measurerPerfEvents);
			desc.setNumberOfMeasurements(50);
			desc.setOptimization("-O3");
			coll.addDescription(desc);

			desc = new MeasurementDescription();
			desc.setKernel(kernel);
			desc.setScheme(scheme);
			desc.setMeasurer(measurerExecutionTime);
			desc.setNumberOfMeasurements(50);
			desc.setOptimization("-O3");
			coll.addDescription(desc);

		}
	}
}
