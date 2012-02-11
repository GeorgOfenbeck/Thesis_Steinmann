package ch.ethz.ruediste.roofline.measurementDriver.appControllers;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;

import com.google.inject.Inject;

/**
 * Controller for the measuring core.
 * 
 * We expect that the source code is not modified externally while the
 * measurement driver runs, thus once we have compiled the core for a
 * measurement, following compilations for the same measurement will yield the
 * same measuring core
 */
public class MeasurementAppController implements IMeasurementFacilility {
	private static Logger log = Logger
			.getLogger(MeasurementAppController.class);

	public final static ConfigurationKey<Boolean> useCachedResultsKey = ConfigurationKey
			.Create(Boolean.class, "useCachedResults",
					"indicates if the cached results should be used", true);

	public final static ConfigurationKey<Boolean> checkCoreKey = ConfigurationKey
			.Create(Boolean.class,
					"checkCore",
					"indicates if the current core has to be the same as the core used to get the cached results",
					true);

	@Inject
	public Configuration configuration;

	@Inject
	public MeasurementResultRepository measurementResultRepository;

	@Inject
	public MeasuringCoreLocationService measuringCoreLocationService;

	@Inject
	public MeasurementService measurementService;

	@Inject
	public MeasurementValidationService measurementValidationService;

	@Inject
	public MeasurementHashRepository measurementHashRepository;

	@Inject
	public HashService hashService;

	private MeasurementHash currentlyCompiledMeasurementHash;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.ruediste.roofline.measurementDriver.appControllers.
	 * IMeasurementFacilility
	 * #measure(ch.ethz.ruediste.roofline.dom.MeasurementDescription, int)
	 */
	public MeasurementResult measure(Measurement measurement,
			int numberOfMeasurements) {

		// add validation measurers
		measurementValidationService.addValidationMeasurers(measurement);

		// prepare the measurement for running
		measurementService.prepareMeasurement(measurement);

		// collect outputs in this collection
		ArrayList<MeasurementRunOutput> outputs = new ArrayList<MeasurementRunOutput>();

		try {
			MeasurementHash measurementHash = hashService
					.getMeasurementHash(measurement);
			log.debug("measuring " + measurementHash);

			log.debug("useCachedResults="
					+ configuration.get(useCachedResultsKey));
			if (
			// should we use cached results?
			configuration.get(useCachedResultsKey)
			// or was the measurement already measured in this run?
					|| measurementHashRepository
							.hasMeasurementBeenSeen(measurementHash)) {
				log.trace("looking in result repository");
				// load stored results
				MeasurementResult cachedResult = measurementResultRepository
						.getMeasurementResult(measurementHash);
				if (
				// were there results?
				cachedResult != null
				// and were they created for the current measuring core?
						&& (!configuration.get(checkCoreKey) || getCoreHash(
								measurement, measurementHash).equals(
								cachedResult.getCoreHash()))) {

					log.trace("found results");
					// use the stored results
					outputs.addAll(cachedResult.getOutputs());
				}
			}

			// do we need more results?
			if (numberOfMeasurements > outputs.size()) {
				log.trace("more results required");
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
				newResult
						.setCoreHash(getCoreHash(measurement, measurementHash));

				// store combined outputs in cache
				measurementResultRepository.store(newResult, measurementHash);
			}
		}
		catch (Exception e) {
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

		result.setResultUids();

		// validate
		measurementValidationService.validate(result);

		return result;
	}

	/**
	 * either gets the core hash from measurementHashToCoreHash or builds the
	 * core and hashes the executable. The core hash is stored in
	 * measurementHashToCoreHash upon return.
	 */
	public CoreHash getCoreHash(Measurement measurement,
			MeasurementHash measurementHash) throws Exception, IOException {

		// get the core hash if it has already been computed
		CoreHash coreHash = measurementHashRepository
				.getCoreHash(measurementHash);

		if (coreHash == null) {
			// build the core
			buildMeasuringCore(measurement, measurementHash);

			// get the core hash if it is known now
			coreHash = measurementHashRepository.getCoreHash(measurementHash);
		}

		if (coreHash == null) {
			// calculate the hash
			coreHash = hashService.hashCurrentlyCompiledMeasuringCore();

			// store the mapping
			measurementHashRepository.setCoreHash(measurementHash, coreHash);
		}
		return coreHash;
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

	public void buildMeasuringCore(Measurement measurement) throws Exception {
		buildMeasuringCore(measurement,
				hashService.getMeasurementHash(measurement));
	}

	public void buildMeasuringCore(Measurement measurement,
			MeasurementHash measurementHash) throws Exception {

		// is the right measurement compiled already?
		if (currentlyCompiledMeasurementHash != null
				&& currentlyCompiledMeasurementHash.equals(measurementHash)) {
			return;
		}

		// is the right core compiled already?
		if (currentlyCompiledMeasurementHash != null
				&& measurementHashRepository.areCoresEqual(
						currentlyCompiledMeasurementHash, measurementHash)) {
			return;
		}

		// prepare the core
		boolean coreChanged = measurementService
				.prepareMeasuringCoreBuilding(measurement);

		if (
		// did we compile before (if we would have, we would know the hash)?
		currentlyCompiledMeasurementHash == null
		// or did the core change?
				|| coreChanged) {
			// build the core
			measurementService.compilePreparedMeasuringCore(measurement);
		}
		else {
			// nothing changed, so the measurement has the same core
			// as the measurement compiled before
			if (currentlyCompiledMeasurementHash != null) {
				measurementHashRepository.setHaveEqualCores(
						currentlyCompiledMeasurementHash, measurementHash);
			}
		}

		// update the compiled measurement hash (either we recompiled, or we did not change anything)
		currentlyCompiledMeasurementHash = measurementHash;
	}
}
