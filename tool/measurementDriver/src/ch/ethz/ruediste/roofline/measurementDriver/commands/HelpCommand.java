package ch.ethz.ruediste.roofline.measurementDriver.commands;

import java.io.InputStream;

import ch.ethz.ruediste.roofline.measurementDriver.Instantiator;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommand;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;

import com.google.inject.Inject;

public class HelpCommand implements ICommand {

	@Inject
	public Instantiator instantiator;

	public String getName() {
		return "help";
	}

	public String getDescription() {
		return "Shows the help text";
	}

	public void execute(String[] args) {
		try {
			// copy help.txt, which is included in the jar, to the standard
			// output
			InputStream helpTextStream = ClassLoader
					.getSystemResourceAsStream("help.txt");
			int ch;
			while ((ch = helpTextStream.read()) > 0) {
				System.out.write(ch);
			}
			helpTextStream.close();
		} catch (Exception e) {
			throw new Error(e);
		}

		System.out.println("\nCommands:");
		instantiator.listNamed(ICommand.class);

		System.out.println("\nMeasurements:");
		instantiator.listNamed(IMeasurement.class);
	}
}
