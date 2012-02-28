package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import java.util.HashMap;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.sharedEntities.*;

/**
 * Keeps track of the measurement hashes and the core hashes
 */
public class MeasurementHashRepository {
	private static Logger log = Logger
			.getLogger(MeasurementHashRepository.class);

	/**
	 * representation of a core
	 */
	private class Core {
		public CoreHash coreHash;
	}

	private MultiValueMap coreToMeasurement = new MultiValueMap();
	private HashMap<MeasurementHash, Core> measurementToCore = new HashMap<MeasurementHash, Core>();

	private HashMap<CoreHash, Core> coreHashToCore = new HashMap<CoreHash, Core>();

	/**
	 * tells this MeasurementHashRepository that the cores of the two
	 * measurements are equal.
	 */
	public void setHaveEqualCores(MeasurementHash a, MeasurementHash b) {
		log.debug(String.format("%s and %s have equal cores", a, b));
		Core coreA = measurementToCore.get(a);
		Core coreB = measurementToCore.get(b);

		if (coreA != null && coreB != null && coreA == coreB) {
			// cores are already equal, nothing to do
			return;
		}

		if (coreA == null && coreB == null) {
			// create new core and associate both measurements
			Core core = new Core();
			coreToMeasurement.put(core, a);
			coreToMeasurement.put(core, b);
			measurementToCore.put(a, core);
			measurementToCore.put(b, core);
			return;
		}

		if (coreA == null && coreB != null) {
			// associate measurement A to core B
			coreToMeasurement.put(coreB, a);
			measurementToCore.put(a, coreB);
			return;
		}

		if (coreA != null && coreB == null) {
			// assosiate measurement B to core A
			coreToMeasurement.put(coreA, b);
			measurementToCore.put(b, coreA);
			return;
		}

		// both measurements have a core already. The two cores need to be merged
		// merge the smaller core into the larger one
		if (coreToMeasurement.size(coreA) > coreToMeasurement.size(coreB)) {
			mergeSecondCoreIntoFirstCore(coreA, coreB);
		}
		else {
			mergeSecondCoreIntoFirstCore(coreB, coreA);
		}
	}

	/**
	 * merges coreB into coreA
	 */
	public void mergeSecondCoreIntoFirstCore(Core coreA, Core coreB)
			throws Error {
		// update the core hash of coreA 
		if (coreB.coreHash != null) {
			if (coreA.coreHash != null
					&& !coreA.coreHash.equals(coreB.coreHash)) {
				throw new Error(
						"measurements set to equal, but core hashes differ");
			}
			coreA.coreHash = coreB.coreHash;
		}

		// coreB has less associated measurements, merge into coreA
		for (Object measurement : coreToMeasurement.getCollection(coreB)) {
			coreToMeasurement.put(coreA, measurement);
			measurementToCore.put((MeasurementHash) measurement, coreA);
		}
		coreToMeasurement.remove(coreB);
	}

	/**
	 * return if the cores of two measurements are equal
	 */
	public boolean areCoresEqual(MeasurementHash a, MeasurementHash b) {
		Core coreA = measurementToCore.get(a);
		Core coreB = measurementToCore.get(b);

		if (coreA == null || coreB == null)
			return false;

		return coreA == coreB;
	}

	public boolean hasMeasurementBeenSeen(MeasurementHash hash) {
		return measurementToCore.containsKey(hash);
	}

	/**
	 * returns the core hash for the given measurement. If the measurement has
	 * not been seen yet, null is returned. If the core hash is not known, null
	 * is returned.
	 */
	public CoreHash getCoreHash(MeasurementHash measurementHash) {
		Core core = measurementToCore.get(measurementHash);
		if (core == null) {
			return null;
		}
		return core.coreHash;
	}

	/**
	 * sets the core hash of a measurement, optionally merging cores
	 */
	public void setCoreHash(MeasurementHash measurementHash, CoreHash coreHash) {
		// get the core for the provided measurement
		Core coreForMeasurement = measurementToCore.get(measurementHash);

		// is the measurement unknown?
		if (coreForMeasurement == null) {
			// create new core and associate measurement
			coreForMeasurement = new Core();
			coreToMeasurement.put(coreForMeasurement, measurementHash);
			measurementToCore.put(measurementHash, coreForMeasurement);
		}

		// get the core for the specified hash
		Core coreForHash = coreHashToCore.get(coreHash);

		// is there NO core with the hash?
		if (coreForHash == null) {
			// just set the core hash of the coreForMeasurement
			coreForMeasurement.coreHash = coreHash;
			coreHashToCore.put(coreHash, coreForMeasurement);
		}
		else {
			// there is already a core for the provided coreHash

			// merge the core of the measurement with the core of the coreHash
			for (Object measurement : coreToMeasurement
					.getCollection(coreForMeasurement)) {
				coreToMeasurement.put(coreForHash, measurement);
				measurementToCore.put((MeasurementHash) measurement,
						coreForHash);
			}
			coreToMeasurement.remove(coreForMeasurement);
		}
	}
}
