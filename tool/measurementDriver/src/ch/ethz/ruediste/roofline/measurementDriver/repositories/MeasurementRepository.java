package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;

import com.google.inject.Inject;

public class MeasurementRepository {
	public final static ConfigurationKey<Boolean> useCachedResultsKey = ConfigurationKey
			.Create(Boolean.class, "useCachedResults",
					"indicates if the cached results should be used", true);

	@Inject
	public MeasurementService measurementService;

	@Inject
	public MeasurementCacheService cacheService;

	@Inject
	public Configuration configuration;

	/**
	 * Return the specified number of measurement results of the specified
	 * measurement. If available, cached values are reused. Otherwise the
	 * measuring core is started
	 */
	public MeasurementResult getMeasurementResults(
			MeasurementDescription measurement,
			int numberOfMeasurements) {

		ArrayList<MeasurerOutputBase> outputs = new ArrayList<MeasurerOutputBase>();

		// check cache
		if (configuration.get(useCachedResultsKey))
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
