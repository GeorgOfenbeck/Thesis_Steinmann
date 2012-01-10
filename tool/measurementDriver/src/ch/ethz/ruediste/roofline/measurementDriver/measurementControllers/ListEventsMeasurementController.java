package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;
import java.io.PrintStream;

import ch.ethz.ruediste.roofline.dom.DummyKernelDescription;
import ch.ethz.ruediste.roofline.dom.ListEventsMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.ListEventsMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.MeasurerOutputBase;
import ch.ethz.ruediste.roofline.dom.PerfEventAttributeDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventDescription;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.ConfigurationKey;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.MeasurementRepository;

import com.google.inject.Inject;

public class ListEventsMeasurementController implements IMeasurementController {

	public static ConfigurationKey<String> architectureKey = ConfigurationKey
			.Create(
					String.class,
					"eventArchitecture",
					"performance event architecture to list events for. see pfmlib.h for a list",
					"PFM_PMU_PERF_EVENT");

	public String getName() {
		return "listEvents";
	}

	public String getDescription() {
		return "lists all events of a given architecture";
	}

	@Inject
	Configuration configuration;

	@Inject
	MeasurementRepository measurementRepository;

	public void measure(String outputName) throws IOException {
		// list all available performance counters
		ListEventsMeasurerDescription measurer = new ListEventsMeasurerDescription();

		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(new DummyKernelDescription());
		measurement.setMeasurer(measurer);
		measurement.setScheme(new SimpleMeasurementSchemeDescription());

		measurement.setOptimization("-O3");
		measurement.addMacro(
				ListEventsMeasurerDescription.architectureMacro,
				configuration.get(architectureKey));

		MeasurementResult result = measurementRepository.getMeasurementResults(
				measurement, 1);

		PrintStream out = new PrintStream("events_"
				+ configuration.get(architectureKey) + ".txt");
		for (MeasurerOutputBase outputBase : result.getOutputs()) {
			ListEventsMeasurerOutput output = (ListEventsMeasurerOutput) outputBase;

			for (PerfEventDescription event : output.getEvents()) {
				out.printf("%s::%s\n%s\n", output.getPmuName(),
						event.getName(), event.getDescription());

				for (PerfEventAttributeDescription attribute : event
						.getAttributes()) {
					out.printf("  %s %s: %s\n", attribute.getAttributeType(),
							attribute.getName(),
							attribute.getDescription());
				}
				out.println();
			}
		}
		out.close();
	}

}
