package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;

import com.google.inject.Inject;

public class PerformanceEventScreeningMeasurementController implements
		IMeasurementController {

	public String getName() {
		return "performanceEventScreening";
	}

	public String getDescription() {
		return "screens the performance events using a set of kernels";
	}

	@Inject
	Configuration configuration;

	@Inject
	MeasurementService measurementService;

	public void measure(String outputName) throws IOException {
		MeasurementResult result;
		{
			MeasurementDescription measurement = new MeasurementDescription();
			measurement.setKernel(new DummyKernelDescription());
			ListEventsMeasurerDescription measurer = new ListEventsMeasurerDescription();
			measurement.setMeasurer(measurer);
			measurement.setScheme(new SimpleMeasurementSchemeDescription());

			result = measurementService.measure(measurement, 1);
		}

		PrintStream out = new PrintStream(outputName + ".txt");
		out.println("<Event>: <Kernel>: <minimal count> <median/min>");

		// iterate over available performance events and attributes
		ListEventsMeasurerOutput output = (ListEventsMeasurerOutput) result
				.getOutputs().get(0);

		for (PmuDescription pmu : output.getPmus()) {
			if (!pmu.getIsPresent()) {
				continue;
			}
			for (PerfEventDescription event : pmu.getEvents()) {
				measure(out, pmu.getPmuName(), event, null);
				/*
				 * for (PerfEventAttributeDescription attribute : event
				 * .getAttributes()) { if
				 * ("UMASK".equals(attribute.getAttributeType()) ||
				 * "MOD_BOOL".equals(attribute .getAttributeType())) {
				 * measure(out, output.getPmuName(), event, attribute);
				 * 
				 * } }
				 */
			}
		}
		out.close();
	}

	private void measure(PrintStream out, String pmuName,
			PerfEventDescription event, PerfEventAttributeDescription attribute) {
		List<Pair<KernelDescriptionBase, String>> kernels = new ArrayList<Pair<KernelDescriptionBase, String>>();

		{
			MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernels.add(Pair.of((KernelDescriptionBase) kernel, "MemoryLoad "
					+ kernel.getBufferSize()));
		}

		{
			MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 4);
			kernels.add(Pair.of((KernelDescriptionBase) kernel, "MemoryLoad "
					+ kernel.getBufferSize()));
		}

		{
			TriadKernelDescription kernel = new TriadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernels.add(Pair.of((KernelDescriptionBase) kernel, "Triad "
					+ kernel.getBufferSize()));
		}

		{
			TriadKernelDescription kernel = new TriadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 4);
			kernels.add(Pair.of((KernelDescriptionBase) kernel, "Triad "
					+ kernel.getBufferSize()));
		}

		{
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.setIterations(1024 * 1024);
			kernel.setUnroll(8);
			kernel.setOperation("ArithmeticOperation_ADD");
			kernels.add(Pair.of((KernelDescriptionBase) kernel, "Arithmetic "
					+ kernel.getIterations()));
		}

		{
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.setIterations(1024 * 1024 * 2);
			kernel.setUnroll(8);
			kernels.add(Pair.of((KernelDescriptionBase) kernel, "Arithmetic "
					+ kernel.getIterations()));
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
			measurement.setKernel(pair.getLeft());
			measurement.setMeasurer(measurer);

			MeasurementResult result = null;
			try {
				result = measurementService.measure(measurement, 20);
			} catch (Throwable e) {
				e.printStackTrace();
			}

			if (result == null) {
				out.printf("%s: %s: failed\n", eventDefinition, pair.getRight());

			} else {
				DescriptiveStatistics statistics = PerfEventMeasurerOutput
						.getStatistics("event", result);

				double min = statistics.getMin();
				out.printf("%s: %s: %g %g\n", eventDefinition, pair.getRight(),
						min, statistics.getPercentile(50)
								/ (min < 0.1 ? 1 : min));

			}
			out.flush();
		}
	}
}
