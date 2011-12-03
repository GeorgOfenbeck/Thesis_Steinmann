package ch.ethz.ruediste.roofline.measurementDriver.baseClasses;

public interface ICommand extends INamed {
	void execute(String args[]);
}
