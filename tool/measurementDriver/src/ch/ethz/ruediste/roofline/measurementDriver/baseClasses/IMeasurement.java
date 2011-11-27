package ch.ethz.ruediste.roofline.measurementDriver.baseClasses;

import java.io.IOException;

public interface IMeasurement {
	void measure(String outputName) throws IOException;
}
