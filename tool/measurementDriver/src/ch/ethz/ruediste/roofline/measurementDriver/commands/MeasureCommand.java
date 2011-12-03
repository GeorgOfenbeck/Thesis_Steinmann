package ch.ethz.ruediste.roofline.measurementDriver.commands;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.Instantiator;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommand;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class MeasureCommand implements ICommand {

	public String getName() {
		return "measure";
	}

	public String getDescription() {
		return "<measurement name> [<output name>] \nExecutes the given measurement";

	}

	@Inject
	public Instantiator instantiator;

	public void execute(String[] args) {

		String measurementName = args[0];

		IMeasurement measurement = instantiator
				.getInstance(Key.get(IMeasurement.class,
						Names.named(measurementName)));

		try {
			measurement.measure(measurementName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

}
