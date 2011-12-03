package ch.ethz.ruediste.roofline.measurementDriver.commands;

import java.io.InputStream;

import ch.ethz.ruediste.roofline.measurementDriver.Instantiator;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommand;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.INamed;

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
		listNamed(ICommand.class);

		System.out.println("\nMeasurements:");
		listNamed(IMeasurement.class);
	}

	private <T extends INamed> void listNamed(Class<T> baseClass) {
		for (Class<? extends T> namedClass : instantiator
				.getBoundClasses(baseClass)) {
			INamed named = instantiator.getInstance(namedClass);
			System.out.printf("%s\t\t%s\n", named.getName(),
					named.getDescription().replace("\n", "\n\t\t"));
		}
	}
}
