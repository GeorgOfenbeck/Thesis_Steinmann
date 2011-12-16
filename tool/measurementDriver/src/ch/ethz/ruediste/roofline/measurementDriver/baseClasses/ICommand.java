package ch.ethz.ruediste.roofline.measurementDriver.baseClasses;

import java.util.List;

public interface ICommand extends INamed {
	void execute(List<String> parsedArgs);
}
