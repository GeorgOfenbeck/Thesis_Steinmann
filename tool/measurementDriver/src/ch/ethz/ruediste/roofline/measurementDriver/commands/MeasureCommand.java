package ch.ethz.ruediste.roofline.measurementDriver.commands;

import java.io.IOException;
import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.IAutoCompletionCommand;
import ch.ethz.ruediste.roofline.measurementDriver.Instantiator;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class MeasureCommand implements IAutoCompletionCommand {

	public String getName() {
		return "measure";
	}

	public String getDescription() {
		return "<measurement name> [<output name>] \nExecutes the given measurement";

	}

	@Inject
	public Instantiator instantiator;

	public void execute(List<String> args) {
		// check if measurement name has been provided
		if (args.size() < 1) {
			System.out.printf("Usage: %s %s\n", getName(), getDescription());
			System.exit(0);
		}

		// get the measurement name
		String measurementName = args.get(0);

		IMeasurement measurement = null;

		// instantiate the measurement
		try {
			measurement = instantiator
					.getInstance(Key.get(IMeasurement.class,
							Names.named(measurementName)));
		} catch (ConfigurationException e) {
			System.out
					.printf("Could not find the measurement named %s\nAvailable Measurements:\n",
							measurementName);
			instantiator.listNamed(IMeasurement.class);
			System.exit(1);
		}

		// get the output name
		String outputName = measurementName;
		if (args.size() >= 2) {
			outputName = args.get(1);
		}

		// perform the measurement
		try {
			measurement.measure(outputName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void doAutoCompletion(String partialWord, List<String> args) {
		if (
		// no measurement has been entered yet
		args.size() == 0
				// part of a measurement has been entered already
				|| (args.size() == 1 && !partialWord.isEmpty())) {
			for (Class<? extends IMeasurement> clazz : instantiator
					.getBoundClasses(IMeasurement.class)) {
				IMeasurement measurement = instantiator.getInstance(clazz);
				if (measurement.getName().startsWith(partialWord)) {
					System.out.println(measurement.getName());
				}
			}
		}
	}

}
