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
import ch.ethz.ruediste.roofline.dom.PmuDescription;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.ConfigurationKey;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.MeasurementRepository;

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
	MeasurementRepository measurementRepository;

	public void measure(String outputName) throws IOException {
		// list all available performance counters
		ListEventsMeasurerDescription measurer = new ListEventsMeasurerDescription();

		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(new DummyKernelDescription());
		measurement.setMeasurer(measurer);
		measurement.setScheme(new SimpleMeasurementSchemeDescription());

		MeasurementResult result = measurementRepository.getMeasurementResults(
				measurement, 1);

		ListEventsMeasurerOutput output = (ListEventsMeasurerOutput) result
				.getOutputs().get(0);

		for (PmuDescription pmu : output.getPmus()) {
			if (!pmu.getIsPresent())
				continue;
			
			if (!pmu.getIsDefaultPmu()){
				System.out.println("The default PMU is "+pmu.getPmuName());
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
