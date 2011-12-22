package ch.ethz.ruediste.roofline.measurementDriver;

import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommand;

public interface IAutoCompletionCommand extends ICommand {
	void doAutoCompletion(String partialWord, List<String> args);
}
