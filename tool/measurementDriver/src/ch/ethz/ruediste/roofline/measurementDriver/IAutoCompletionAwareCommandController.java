package ch.ethz.ruediste.roofline.measurementDriver;

import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommandController;

public interface IAutoCompletionAwareCommandController extends ICommandController {
	void doAutoCompletion(String partialWord, List<String> args);
}
