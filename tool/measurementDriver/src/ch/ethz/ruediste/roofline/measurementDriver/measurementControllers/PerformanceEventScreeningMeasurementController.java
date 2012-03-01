package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.SystemInfoService;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.ArithmeticKernel.ArithmeticOperation;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.*;

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
	MeasurementAppController measurementAppController;

	@Inject
	SystemInfoService systemInfoService;

	public void measure(String outputName) throws IOException {
		PrintStream out = new PrintStream(outputName + ".txt");
		out.println("<Event>: <Kernel>: <minimal count> <median/min>");

		for (PmuDescription pmu : systemInfoService.getPresentPmus()) {
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
		List<Pair<KernelBase, String>> kernels = new ArrayList<Pair<KernelBase, String>>();

		{
			MemoryKernel kernel = new MemoryKernel();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernels.add(Pair.of((KernelBase) kernel,
					"MemoryLoad " + kernel.getBufferSize()));
		}

		{
			MemoryKernel kernel = new MemoryKernel();
			kernel.setBufferSize(1024 * 1024 * 4);
			kernels.add(Pair.of((KernelBase) kernel,
					"MemoryLoad " + kernel.getBufferSize()));
		}

		{
			TriadKernel kernel = new TriadKernel();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernels.add(Pair.of((KernelBase) kernel,
					"Triad " + kernel.getBufferSize()));
		}

		{
			TriadKernel kernel = new TriadKernel();
			kernel.setBufferSize(1024 * 1024 * 4);
			kernels.add(Pair.of((KernelBase) kernel,
					"Triad " + kernel.getBufferSize()));
		}

		{
			ArithmeticKernel kernel = new ArithmeticKernel();
			kernel.setIterations(1024 * 1024);
			kernel.setUnroll(8);
			kernel.setOperation(ArithmeticOperation.ArithmeticOperation_ADD);
			kernels.add(Pair.of((KernelBase) kernel,
					"Arithmetic " + kernel.getIterations()));
		}

		{
			ArithmeticKernel kernel = new ArithmeticKernel();
			kernel.setIterations(1024 * 1024 * 2);
			kernel.setUnroll(8);
			kernels.add(Pair.of((KernelBase) kernel,
					"Arithmetic " + kernel.getIterations()));
		}

		for (Pair<KernelBase, String> pair : kernels) {
			PerfEventMeasurer measurer = new PerfEventMeasurer();
			String eventDefinition = pmuName + "::" + event.getName();
			if (attribute != null) {
				eventDefinition += ":" + attribute.getName();
			}
			measurer.addEvent("event", eventDefinition);

			Measurement measurement = new Measurement();
			Workload workload = new Workload();
			workload.setKernel(pair.getLeft());
			workload.setMeasurerSet(new MeasurerSet(measurer));

			MeasurementResult result = null;
			try {
				result = measurementAppController.measure(measurement, 20);
			}
			catch (Throwable e) {
				e.printStackTrace();
			}

			if (result == null) {
				out.printf("%s: %s: failed\n", eventDefinition, pair.getRight());

			}
			else {
				DescriptiveStatistics statistics = measurer.getStatistics(
						"event", result);

				double min = statistics.getMin();
				out.printf("%s: %s: %g %g\n", eventDefinition, pair.getRight(),
						min, statistics.getPercentile(50)
								/ (min < 0.1 ? 1 : min));

			}
			out.flush();
		}
	}
}
