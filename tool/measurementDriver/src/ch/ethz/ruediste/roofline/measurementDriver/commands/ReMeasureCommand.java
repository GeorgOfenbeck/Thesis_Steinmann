package ch.ethz.ruediste.roofline.measurementDriver.commands;

import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.IAutoCompletionCommand;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;

import com.google.inject.Inject;

public class ReMeasureCommand implements IAutoCompletionCommand {

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

	public void execute(List<String> args) {
		configuration.set(MeasurementAppController.useCachedResultsKey, false);

		measureCommand.execute(args);
	}

	public void doAutoCompletion(String partialWord, List<String> args) {
		measureCommand.doAutoCompletion(partialWord, args);
	}

}
