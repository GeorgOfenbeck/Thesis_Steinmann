package ch.ethz.ruediste.roofline.measurementDriver.appControllers;

import java.io.IOException;
import java.util.*;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.MeasurementResultRepository;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;

import com.google.inject.Inject;

/**
 * Controller for the measuring core. Assumes exclusive access to the core.
 */
public class MeasurementAppController implements IMeasurementFacilility {

	public final static ConfigurationKey<Boolean> useCachedResultsKey = ConfigurationKey
			.Create(Boolean.class, "useCachedResults",
					"indicates if the cached results should be used", true);

	@Inject
	public Configuration configuration;

	@Inject
	public MeasurementResultRepository measurementResultRepository;

	@Inject
	public MeasuringCoreLocationService measuringCoreLocationService;

	@Inject
	public MeasurementService measurementService;

	@Inject
	public HashService hashService;

	/**
	 * contains the measurement which the core is currently compiled for
	 */
	private MeasurementHash currentlyCompiledMeasurementHash;

	/**
	 * contains the hash of the currently compiled core
	 */
	private CoreHash currentlyCompiledCoreHash;

	/**
	 * contains all measurement hashes which have been measured already in this
	 * session
	 */
	private final HashSet<MeasurementHash> measuredMeasurements = new HashSet<MeasurementHash>();

	/**
	 * Maps measurement hashes to core hashes for measurements which have
	 * already been seen in this session. We expect that the source code is not
	 * modified externally while the measurement driver runs, thus once we have
	 * compiled the core for a measurement, following compilations for the same
	 * measurement will yield the same measuring core
	 */
	private final HashMap<MeasurementHash, CoreHash> measurementHashToCoreHash = new HashMap<MeasurementHash, CoreHash>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.ruediste.roofline.measurementDriver.appControllers.
	 * IMeasurementFacilility
	 * #measure(ch.ethz.ruediste.roofline.dom.MeasurementDescription, int)
	 */
	public MeasurementResult measure(MeasurementDescription measurement,
			int numberOfMeasurements) {

		ArrayList<MeasurerOutputBase> outputs = new ArrayList<MeasurerOutputBase>();

		try {
			MeasurementHash measurementHash = hashService
					.getMeasurementHash(measurement);

			// get the hash of the core for the measurement
			CoreHash coreHash = getCoreHash(measurement, measurementHash);

			if (
			// should we use cached results?
			configuration.get(useCachedResultsKey)
					// or was the measurement alredy measured in this run?
					|| measuredMeasurements.contains(measurementHash)) {
				// load stored results
				MeasurementResult cachedResult = measurementResultRepository
						.getMeasurementResult(measurementHash);
				if (
				// were there results?
				cachedResult != null
						// and were they created for the current measuring core?
						&& coreHash.equals(cachedResult.getCoreHash())) {

					// use the stored results
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
				MeasurementResult newResult = performMeasurement(command,
						measurementHash);

				// combine outputs
				newResult.getOutputs().addAll(outputs);

				// overwrite outputs with combined results
				outputs.clear();
				outputs.addAll(newResult.getOutputs());

				// set the core hash of the results
				newResult.setCoreHash(coreHash);

				// store combined outputs in cache
				measurementResultRepository.store(newResult, measurementHash);
			}
		} catch (Exception e) {
			throw new Error("error occured during measurement", e);
		}

		if (outputs.size() < numberOfMeasurements) {
			throw new Error("unable to retrieve enough results");
		}

		// create the result to be returned
		MeasurementResult result = new MeasurementResult();
		result.setMeasurement(measurement);

		// add exactly the number of measurements required to the output
		// it might be that there were more results in the repository than
		// needed
		for (int i = 0; i < numberOfMeasurements; i++) {
			result.getOutputs().add(outputs.get(i));
		}

		return result;
	}

	/**
	 * either gets the core hash from measurementHashToCoreHash or builds the
	 * core and hashes the executable. The core hash is stored in
	 * measurementHashToCoreHash upon return.
	 */
	public CoreHash getCoreHash(MeasurementDescription measurement,
			MeasurementHash measurementHash) throws Exception, IOException {
		// get the core hash if it has already been seen in this session
		CoreHash coreHash = measurementHashToCoreHash.get(measurementHash);

		if (coreHash == null) {
			// build the core
			buildMeasuringCore(measurement, measurementHash);

			// get the hash of the measuring core
			coreHash = currentlyCompiledCoreHash;
		}
		return coreHash;
	}

	public DescriptiveStatistics getStatistics(String event,
			KernelDescriptionBase kernel, int numberOfResults) {
		PerfEventMeasurerDescription measurer = new PerfEventMeasurerDescription();
		measurer.addEvent("event", event);

		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(kernel);
		measurement.setMeasurer(measurer);
		measurement.setScheme(new SimpleMeasurementSchemeDescription());

		MeasurementResult result = measure(measurement, 10);

		return PerfEventMeasurerOutput.getStatistics("event", result);
	}

	public MeasurementResult performMeasurement(MeasurementCommand command)
			throws Exception {
		return performMeasurement(command,
				hashService.getMeasurementHash(command.getMeasurement()));
	}

	public MeasurementResult performMeasurement(MeasurementCommand command,
			MeasurementHash measurementHash) throws Exception {
		// make sure the core is built
		buildMeasuringCore(command.getMeasurement(), measurementHash);

		// perform the measurement
		MeasurementResult result = measurementService.runMeasuringCore(command);

		return result;
	}

	public void buildMeasuringCore(MeasurementDescription measurement)
			throws Exception {
		buildMeasuringCore(measurement,
				hashService.getMeasurementHash(measurement));
	}

	/**
	 * after return, the currently built core hash is always known
	 */
	public void buildMeasuringCore(MeasurementDescription measurement,
			MeasurementHash measurementHash) throws Exception {

		// check if the measuring core is already compiled for the measurement
		if (measurementHash.equals(currentlyCompiledMeasurementHash)) {
			return;
		}

		// prepare the core
		boolean coreChanged = measurementService
				.perpareMeasuringCoreBuilding(measurement);

		// do we need to update the core?
		if (currentlyCompiledCoreHash == null || coreChanged) {
			// build the core
			measurementService.buildPreparedMeasuringCore(measurement);

			// update the core hash
			currentlyCompiledCoreHash = hashService.getMeasuringCoreHash();
		}

		// set the compiled measurement
		currentlyCompiledMeasurementHash = measurementHash;

		// store the current mapping
		measurementHashToCoreHash.put(currentlyCompiledMeasurementHash,
				currentlyCompiledCoreHash);
	}
}
