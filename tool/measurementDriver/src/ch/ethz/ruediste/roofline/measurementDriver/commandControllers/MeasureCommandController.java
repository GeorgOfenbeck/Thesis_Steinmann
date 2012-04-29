package ch.ethz.ruediste.roofline.measurementDriver.commandControllers;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.util.Instantiator;

import com.google.inject.*;
import com.google.inject.name.Names;

public class MeasureCommandController implements
		IAutoCompletionAwareCommandController {
	private static Logger log = Logger
			.getLogger(MeasureCommandController.class);

	public String getName() {
		return "measure";
	}

	public String getDescription() {
		return "<measurement controller name> [<output name>] \nExecutes the given measurement controller";

	}

	@Inject
	public Instantiator instantiator;

	public void execute(List<String> args) {
		// check if measurement name has been provided
		if (args.size() < 1) {
			log.fatal(String.format("Usage: %s %s\n", getName(),
					getDescription()));
			System.exit(0);
		}

		// get the measurement name
		String measurementName = args.get(0);

		IMeasurementController measurement = null;

		// instantiate the measurement
		try {
			measurement = instantiator
					.getInstance(Key.get(IMeasurementController.class,
							Names.named(measurementName)));
		}
		catch (ConfigurationException e) {
			System.out
					.printf("Could not find the measurement named %s\nAvailable Measurements:\n",
							measurementName);
			instantiator.listNamed(IMeasurementController.class);
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
		}
		catch (IOException e) {
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
			for (Class<? extends IMeasurementController> clazz : instantiator
					.getBoundClasses(IMeasurementController.class)) {
				IMeasurementController measurement = instantiator
						.getInstance(clazz);
				if (measurement.getName().startsWith(partialWord)) {
					System.out.println(measurement.getName());
				}
			}
		}
	}

}
