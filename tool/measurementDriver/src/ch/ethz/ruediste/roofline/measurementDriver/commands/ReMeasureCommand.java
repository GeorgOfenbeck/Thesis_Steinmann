package ch.ethz.ruediste.roofline.measurementDriver.commands;

import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommand;

import com.google.inject.Inject;

public class ReMeasureCommand implements ICommand {

	@Inject
	public MeasureCommand measureCommand;

	@Inject
	public Configuration configuration;

	public String getName() {
		return "remeasure";
	}

	public String getDescription() {
		return "<measurement name> [<output name>] \nExecutes the given measurement, overwriting the cached results";
	}

	public void execute(String[] args) {
		configuration.set(MeasurementAppController.useCachedResultsKey, false);

		measureCommand.execute(args);
	}

}
