package ch.ethz.ruediste.roofline.measurementDriver.appControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.MeasurementCommand;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MeasurementAppController {

	@Inject
	public MeasurementService measurementService;

	public MeasurementResult measure(MeasurementDescription measurement,
			int numberOfMeasurements) throws IOException {

		// create command
		MeasurementCommand command = new MeasurementCommand();
		command.setMeasurement(measurement);
		command.setNumberOfMeasurements(numberOfMeasurements);

		return measurementService.measure(command);

		// check cache

		// if (found in cache)
		// return value found

		// create measurement command

		// perform measurement

		// store measurement in cache
	}

}
