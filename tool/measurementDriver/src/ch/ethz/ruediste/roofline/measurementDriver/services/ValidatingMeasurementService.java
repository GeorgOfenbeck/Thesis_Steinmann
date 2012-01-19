package ch.ethz.ruediste.roofline.measurementDriver.services;

import ch.ethz.ruediste.roofline.dom.*;

import com.google.inject.Inject;

public class ValidatingMeasurementService {
	@Inject
	MeasurementService measurementService;

	public MeasurementResult measure(MeasurementDescription measurement,
			int numberOfMeasurements) {

		// TODO: Validation
		return measurementService.measure(measurement, numberOfMeasurements);
	}
}
