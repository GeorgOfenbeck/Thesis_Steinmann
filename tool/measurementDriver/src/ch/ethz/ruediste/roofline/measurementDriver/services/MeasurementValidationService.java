package ch.ethz.ruediste.roofline.measurementDriver.services;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;

import com.google.inject.Inject;

public class MeasurementValidationService {
	public final static ConfigurationKey<Boolean> validationKey = ConfigurationKey
			.Create(Boolean.class, "validation",
					"if true, perform validation", true);

	@Inject
	Configuration configuration;

	public void addValidationMeasurers(MeasurementDescription measurement) {
		// skip validation if disabled
		if (!configuration.get(validationKey)) {
			return;
		}

		PerfEventMeasurerDescription perfEventMeasurerDescription = new PerfEventMeasurerDescription();
		perfEventMeasurerDescription.addEvent("contextSwitches",
				"perf::PERF_COUNT_SW_CONTEXT_SWITCHES");

		measurement.addAdditionalMeasurer(perfEventMeasurerDescription);
		measurement.getValidationData().setPerfEventMeasurer(
				perfEventMeasurerDescription);
	}
}
