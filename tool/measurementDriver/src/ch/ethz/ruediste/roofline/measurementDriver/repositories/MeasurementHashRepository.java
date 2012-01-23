package ch.ethz.ruediste.roofline.measurementDriver.repositories;

import java.util.HashMap;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.*;

public class MeasurementHashRepository {
	private static Logger log = Logger
			.getLogger(MeasurementHashRepository.class);

	private class Core {
		public CoreHash coreHash;
	}

	private MultiValueMap coreToMeasurement = new MultiValueMap();
	private HashMap<MeasurementHash, Core> measurementToCore = new HashMap<MeasurementHash, Core>();

	public void setHaveEqualCores(MeasurementHash a, MeasurementHash b) {
		log.debug(String.format("%s and %s have equal cores", a, b));
		Core coreA = measurementToCore.get(a);
		Core coreB = measurementToCore.get(b);

		if (coreA == null && coreB == null) {
			// create new core and associate both measurements
			Core core = new Core();
			coreToMeasurement.put(core, a);
			coreToMeasurement.put(core, b);
			measurementToCore.put(a, core);
			measurementToCore.put(b, core);
		}
		else if (coreA == null && coreB != null) {
			// associate measurement A to core B
			coreToMeasurement.put(coreB, a);
			measurementToCore.put(a, coreB);
		}
		else if (coreA != null && coreB == null) {
			// assosiate measurement B to core A
			coreToMeasurement.put(coreA, b);
			measurementToCore.put(b, coreA);
		}
		else if (coreToMeasurement.size(coreA) > coreToMeasurement.size(coreB)) {
			// coreB has less associated measurements, into coreA
			for (Object m : coreToMeasurement.getCollection(coreB)) {
				coreToMeasurement.put(coreA, m);
				measurementToCore.put((MeasurementHash) m, coreA);
			}
			coreToMeasurement.remove(coreB);
		}
		else {
			// coreA has less associated measurements, into coreB
			for (Object m : coreToMeasurement.getCollection(coreA)) {
				coreToMeasurement.put(coreB, m);
				measurementToCore.put((MeasurementHash) m, coreB);
			}
			coreToMeasurement.remove(coreA);
		}
	}

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

	public CoreHash getCoreHash(MeasurementHash measurementHash) {
		Core core = measurementToCore.get(measurementHash);
		if (core == null) {
			return null;
		}
		return core.coreHash;
	}

	public void setCoreHash(MeasurementHash measurementHash, CoreHash coreHash) {
		Core core = measurementToCore.get(measurementHash);

		// is the measurement unknown?
		if (core == null) {
			// create new core and associate measurement
			core = new Core();
			coreToMeasurement.put(core, measurementHash);
			measurementToCore.put(measurementHash, core);
		}

		// set the core hash
		core.coreHash = coreHash;
	}
}