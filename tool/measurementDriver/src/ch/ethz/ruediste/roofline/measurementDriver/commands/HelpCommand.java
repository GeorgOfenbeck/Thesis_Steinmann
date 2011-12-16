package ch.ethz.ruediste.roofline.measurementDriver.commands;

import java.io.InputStream;
import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.ConfigurationKeyBase;
import ch.ethz.ruediste.roofline.measurementDriver.Instantiator;
import ch.ethz.ruediste.roofline.measurementDriver.Pair;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommand;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;

import com.google.inject.Inject;

public class HelpCommand implements ICommand {

	@Inject
	public Instantiator instantiator;

	@Inject
	public Configuration configuration;

	public String getName() {
		return "help";
	}

	public String getDescription() {
		return "Shows the help text";
	}

	public void execute(List<String> args) {
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
		System.out.println("*********");

		instantiator.listNamed(ICommand.class);

		System.out.println("\nMeasurements:");
		System.out.println("*************");
		instantiator.listNamed(IMeasurement.class);

		System.out.println("\nConfiguration Options:");
		System.out.println("**********************");

		for (Pair<Class<?>, ConfigurationKeyBase> entry : configuration
				.getConfigurationKeys("ch.ethz.ruediste.roofline.measurementDriver")) {
			System.out.printf("%s = %s   (=>%s / %s)\n\t%s\n",
					// key
					entry.getSecond().getKey(),
					// current value
					configuration.get(entry.getSecond()),
					// declaring class
					entry.getFirst().getSimpleName(),
					// type of the value
					entry.getSecond().getValueType().getSimpleName(),
					// description
					entry.getSecond()
							.getDescription()
							.replace("\n", "\t\n"));
		}
	}
}
