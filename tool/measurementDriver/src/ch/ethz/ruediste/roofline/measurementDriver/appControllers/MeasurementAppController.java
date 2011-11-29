package ch.ethz.ruediste.roofline.measurementDriver.appControllers;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.dom.MeasurementCommand;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.MeasurerOutputBase;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementCacheService;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MeasurementAppController {

	@Inject
	public MeasurementService measurementService;

	@Inject
	public MeasurementCacheService cacheService;

	public MeasurementResult measure(MeasurementDescription measurement,
			int numberOfMeasurements) {

		ArrayList<MeasurerOutputBase> outputs = new ArrayList<MeasurerOutputBase>();

		// check cache
		{
			MeasurementResult cachedResult = cacheService
					.loadFromCache(measurement);
			if (cachedResult != null) {
				outputs.addAll(cachedResult.getOutputs());
			}
		}

		// do we need more results?
		if (numberOfMeasurements > outputs.size()) {
			// create measurement command
			MeasurementCommand command = new MeasurementCommand();
			command.setMeasurement(measurement);
			command.setNumberOfMeasurements(numberOfMeasurements
					- outputs.size());

			// perform measurement
			MeasurementResult newResult = measurementService.measure(command);

			// combine outputs
			newResult.getOutputs().addAll(outputs);

			// overwrite outputs with combined results
			outputs.clear();
			outputs.addAll(newResult.getOutputs());

			// store combined outputs in cache
			cacheService.storeInCache(newResult);
		}

		if (outputs.size() < numberOfMeasurements) {
			throw new Error("unable to retrieve enough results");
		}

		// create the result to be returned
		MeasurementResult result = new MeasurementResult();
		result.setMeasurement(measurement);
		for (int i = 0; i < numberOfMeasurements; i++) {
			result.getOutputs().add(outputs.get(i));
		}

		return result;
	}

}