package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.io.File;

import ch.ethz.ruediste.roofline.measurementDriver.*;

import com.google.inject.Inject;

public class MeasuringCoreLocationService {
	public static final ConfigurationKey<String> measuringCorePathKey = ConfigurationKey
			.Create(String.class, "measurement.corePath",
					"Path to the measuring core", ".");

	@Inject
	Configuration configuration;

	public File getMeasuringCoreDir() throws Error {
		// loading the measuring Core directory
		File result = new File(configuration.get(measuringCorePathKey));

		// check if the core directory exists
		if (!result.exists()) {
			throw new Error(
					"Could not find the measuring core. The configured file is: "
							+ result.getAbsolutePath());
		}
		return result;
	}

	public File getBuildDir() {
		File result = new File(getMeasuringCoreDir(), "build");

		if (!result.exists()) {
			throw new Error("Could not find the measuring core build dir: "
					+ result.getAbsolutePath());
		}
		return result;
	}

	public File getMeasuringCoreParentExecutable() {
		File result = new File(getBuildDir(), "measuringCore");
		if (!result.exists()) {
			throw new Error("could not find measuringCore: "
					+ result.getAbsolutePath());
		}
		return result;
	}

	public File getMeasuringCoreChildExecutable() {
		File result = new File(getBuildDir(), "childProcess");
		if (!result.exists()) {
			throw new Error("could not find measuringCore: "
					+ result.getAbsolutePath());
		}
		return result;
	}
}
