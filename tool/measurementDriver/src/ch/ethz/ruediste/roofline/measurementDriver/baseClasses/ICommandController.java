package ch.ethz.ruediste.roofline.measurementDriver.baseClasses;

import java.util.List;

public interface ICommandController extends INamed {
	void execute(List<String> parsedArgs);
}
