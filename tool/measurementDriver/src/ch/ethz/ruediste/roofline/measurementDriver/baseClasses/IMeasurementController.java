package ch.ethz.ruediste.roofline.measurementDriver.baseClasses;

import java.io.IOException;

public interface IMeasurementController extends INamed {
	void measure(String outputName) throws IOException;
}
