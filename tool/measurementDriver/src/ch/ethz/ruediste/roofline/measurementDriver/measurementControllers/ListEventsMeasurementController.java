package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.*;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.SystemInfoRepository;

import com.google.inject.Inject;

public class ListEventsMeasurementController implements IMeasurementController {
	public String getName() {
		return "listEvents";
	}

	public String getDescription() {
		return "lists all events avaiable on the machine";
	}

	@Inject
	Configuration configuration;

	@Inject
	MeasurementAppController measurementAppController;

	@Inject
	SystemInfoRepository pmuRepository;

	public void measure(String outputName) throws IOException {

		for (PmuDescription pmu : pmuRepository.getPresentPmus()) {

			System.out.printf(
					"present PMU: %s counters: %d fixed counters: %d\n",
					pmu.getPmuName(), pmu.getNumberOfCounters(),
					pmu.getNumberOfFixedCounters());

			if (!pmu.getIsDefaultPmu()) {
				System.out.println("Default PMU: " + pmu.getPmuName());
			}

			PrintStream out = new PrintStream("events_" + pmu.getPmuName()
					+ ".txt");

			for (PerfEventDescription event : pmu.getEvents()) {
				out.printf("%s::%s\n%s\n", pmu.getPmuName(), event.getName(),
						event.getDescription());

				for (PerfEventAttributeDescription attribute : event
						.getAttributes()) {
					out.printf("  %s %s: %s\n", attribute.getAttributeType(),
							attribute.getName(), attribute.getDescription());
				}
				out.println();
			}

			out.close();
		}
	}
}
