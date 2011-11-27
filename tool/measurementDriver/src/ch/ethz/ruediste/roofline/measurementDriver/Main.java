package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.measurements.VarianceMeasurement;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class Main {

	public static void main(String args[]) throws IOException {

		if (args.length != 1 && args.length != 2) {
			System.out
					.println("Usage: measuringDriver measurementName [outputName]");
		}

		String measurementName = args[0];
		String outputName = measurementName;
		if (args.length == 2) {
			outputName = args[1];
		}

		Injector injector = Guice.createInjector(new MainModule());

		IMeasurement measurement = injector
				.getInstance(Key.get(VarianceMeasurement.class,
						Names.named(measurementName)));

		measurement.measure(outputName);

	}
}
