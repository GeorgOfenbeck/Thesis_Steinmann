package ch.ethz.ruediste.roofline.measurementDriver.measurementSeries;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.DummyKernelDescription;
import ch.ethz.ruediste.roofline.dom.KernelDescriptionBase;
import ch.ethz.ruediste.roofline.dom.ListEventsMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.ListEventsMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.MeasurerOutputBase;
import ch.ethz.ruediste.roofline.dom.MemoryLoadKernelDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventAttributeDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.dom.TriadKernelDescription;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.Pair;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementSeries;

import com.google.inject.Inject;

public class PerformanceEventScreeningMeasurementSeries implements IMeasurementSeries {

	public String getName() {
		return "performanceEventScreening";
	}

	public String getDescription() {
		return "screens the performance events using a set of kernels";
	}

	@Inject
	Configuration configuration;

	@Inject
	MeasurementAppController measurementAppController;

	public void measure(String outputName) throws IOException {
		MeasurementResult result;
		{
			ListEventsMeasurerDescription measurer = new ListEventsMeasurerDescription();

			MeasurementDescription measurement = new MeasurementDescription();
			measurement.setKernel(new DummyKernelDescription());
			measurement.setMeasurer(measurer);
			measurement.setScheme(new SimpleMeasurementSchemeDescription());
			measurement.addMacro(
					ListEventsMeasurerDescription.architectureMacro,
					configuration
							.get(ListEventsMeasurementSeries.architectureKey));

			result = measurementAppController.measure(
					measurement, 1);
		}

		PrintStream out = new PrintStream(outputName + ".txt");
		out.println("<Event>: <Kernel>: <minimal count> <median/min>");

		// iterate over available performance events and attributes
		for (MeasurerOutputBase outputBase : result.getOutputs()) {
			ListEventsMeasurerOutput output = (ListEventsMeasurerOutput) outputBase;

			for (PerfEventDescription event : output.getEvents()) {
				measure(out, output.getPmuName(), event, null);
				for (PerfEventAttributeDescription attribute : event
						.getAttributes()) {
					if ("UMASK".equals(attribute.getAttributeType())
							|| "MOD_BOOL".equals(attribute
									.getAttributeType())) {
						measure(out, output.getPmuName(), event, attribute);

					}
				}
			}
		}
		out.close();
	}

	private void measure(PrintStream out, String pmuName,
			PerfEventDescription event,
			PerfEventAttributeDescription attribute) {
		List<Pair<KernelDescriptionBase, String>> kernels = new ArrayList<Pair<KernelDescriptionBase, String>>();

		{
			MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 20);
			kernels.add(Pair.create((KernelDescriptionBase) kernel,
					"MemoryLoad " + kernel.getBufferSize()));
		}

		{
			MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 40);
			kernels.add(Pair.create((KernelDescriptionBase) kernel,
					"MemoryLoad " + kernel.getBufferSize()));
		}

		{
			TriadKernelDescription kernel = new TriadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 20);
			kernels.add(Pair.create((KernelDescriptionBase) kernel,
					"Triad " + kernel.getBufferSize()));
		}

		{
			TriadKernelDescription kernel = new TriadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 40);
			kernels.add(Pair.create((KernelDescriptionBase) kernel,
					"Triad " + kernel.getBufferSize()));
		}

		for (Pair<KernelDescriptionBase, String> pair : kernels) {
			PerfEventMeasurerDescription measurer = new PerfEventMeasurerDescription();
			String eventDefinition = pmuName + "::" + event.getName();
			if (attribute != null) {
				eventDefinition += ":" + attribute.getName();
			}
			measurer.addEvent("event", eventDefinition);

			MeasurementDescription measurement = new MeasurementDescription();
			measurement.setScheme(new SimpleMeasurementSchemeDescription());
			measurement.setKernel(pair.getFirst());
			measurement.setMeasurer(measurer);

			MeasurementResult result = measurementAppController.measure(
					measurement, 20);

			DescriptiveStatistics statistics = PerfEventMeasurerOutput
					.getStatistics("event", result);

			out.printf("%s: %s: %g %g\n", eventDefinition,
					pair.getSecond(),
					statistics.getMin(),
					statistics.getPercentile(50) / statistics.getMin());

			out.flush();

		}
	}
}
