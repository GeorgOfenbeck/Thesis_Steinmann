package ch.ethz.ruediste.roofline.measurementDriver.appControllers;

import ch.ethz.ruediste.roofline.dom.*;

public interface IMeasurementFacilility {

	/**
	 * Return the specified number of measurement results of the specified
	 * measurement. If available, cached values are reused. Otherwise the
	 * measuring core is started
	 */
	public MeasurementResult measure(Measurement measurement,
			int numberOfMeasurements);

}