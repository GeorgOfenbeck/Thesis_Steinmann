package ch.ethz.ruediste.roofline.measurementDriver.appControllers;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.repositories.MeasurementResultRepository;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.repositories.*;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.services.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;

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
	public MeasuringCoreService measuringCoreService;

	@Inject
	public MeasurementService measurementService;

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
	public MeasurementResult measure(Measurement measurement, int numberOfRuns) {

		// prepare the measurement for running
		measurementService.prepareMeasurement(measurement);

		// collect outputs in this collection
		ArrayList<MeasurementRunOutput> runOutputs = new ArrayList<MeasurementRunOutput>();

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
				MeasurementResult loadedResult = measurementResultRepository
						.getMeasurementResult(measurementHash);
				if (
				// were there results?
				loadedResult != null
				// and were they created for the current measuring core?
						&& (!configuration.get(checkCoreKey) || getCoreHash(
								measurement, measurementHash).equals(
								loadedResult.getCoreHash()))) {

					log.trace("found results");
					// use the stored results
					runOutputs.addAll(loadedResult.getRunOutputs());
				}
			}

			// do we need more results?
			if (numberOfRuns > runOutputs.size()) {
				log.trace("more results required");
				// create measurement command
				MeasurementCommand command = new MeasurementCommand();
				command.setMeasurement(measurement);
				command.setRunCount(numberOfRuns - runOutputs.size());

				// perform measurement
				MeasurementResult newResult = performMeasurement(command,
						measurementHash);

				// combine outputs
				newResult.getRunOutputs().addAll(runOutputs);

				// overwrite outputs with combined results
				runOutputs.clear();
				runOutputs.addAll(newResult.getRunOutputs());

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

		if (runOutputs.size() < numberOfRuns) {
			throw new Error("unable to retrieve enough results");
		}

		// create the result to be returned
		MeasurementResult result = new MeasurementResult();
		result.setMeasurement(measurement);

		// add exactly the number of measurements required to the output
		// it might be that there were more results in the repository than
		// needed
		for (int i = 0; i < numberOfRuns; i++) {
			result.getRunOutputs().add(runOutputs.get(i));
		}

		result.setResultUids();

		return result;
	}

	/**
	 * either gets the core hash from measurementHashToCoreHash or builds the
	 * core and hashes the executable. The core hash is stored in
	 * measurementHashToCoreHash upon return.
	 */
	private CoreHash getCoreHash(Measurement measurement,
			MeasurementHash measurementHash) throws Exception, IOException {

		// get the core hash if it has already been computed
		CoreHash coreHash = measurementHashRepository
				.getCoreHash(measurementHash);

		if (coreHash == null) {
			// build the core
			buildMeasuringCore(measurement, measurementHash);

			// during the build process, two cores could be merged. This could 
			// cause the core hash to be known by now
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

	private MeasurementResult performMeasurement(MeasurementCommand command,
			MeasurementHash measurementHash) throws Exception {

		// make sure the core is built
		buildMeasuringCore(command.getMeasurement(), measurementHash);

		// perform the measurement
		MeasurementResult result = measuringCoreService
				.runMeasuringCore(command);

		return result;
	}

	private void buildMeasuringCore(Measurement measurement,
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
		boolean coreChanged = measuringCoreService
				.prepareMeasuringCoreBuilding(measurement);

		if (
		// did we compile before? (currentlyCompiledMeasurement non null after first compilation)
		currentlyCompiledMeasurementHash == null
		// or did the core change?
				|| coreChanged) {
			// build the core
			measuringCoreService.compilePreparedMeasuringCore(measurement);
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
