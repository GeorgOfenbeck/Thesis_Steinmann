package ch.ethz.ruediste.roofline.measurementDriver.commandControllers;

import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.IAutoCompletionAwareCommandController;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.MeasurementRepository;

import com.google.inject.Inject;

public class ReMeasureCommandController implements IAutoCompletionAwareCommandController {

	@Inject
	public MeasureCommandController measureCommand;

	@Inject
	public Configuration configuration;

	public String getName() {
		return "remeasure";
	}

	public String getDescription() {
		return "<measurement name> [<output name>] \nExecutes the given measurement, overwriting the cached results";
	}

	public void execute(List<String> args) {
		configuration.set(MeasurementRepository.useCachedResultsKey, false);

		measureCommand.execute(args);
	}

	public void doAutoCompletion(String partialWord, List<String> args) {
		measureCommand.doAutoCompletion(partialWord, args);
	}

}