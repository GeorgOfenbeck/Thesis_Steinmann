package ch.ethz.ruediste.roofline.measurementDriver.commandControllers;

import java.io.InputStream;
import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.ClassFinder;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.ConfigurationKeyBase;
import ch.ethz.ruediste.roofline.measurementDriver.Instantiator;
import ch.ethz.ruediste.roofline.measurementDriver.Pair;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommandController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;

import com.google.inject.Inject;

/**
 * The help command prints the help text to the standard output. This includes a
 * list of commands, measurement series and available configuration flags.
 * 
 */
public class HelpCommandController implements ICommandController {

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

		instantiator.listNamed(ICommandController.class);

		System.out.println("\nMeasurement Series:");
		System.out.println("*************");
		instantiator.listNamed(IMeasurementController.class);

		System.out.println("\nConfiguration Flags:");
		System.out.println("**********************");

		for (Pair<Class<?>, ConfigurationKeyBase> entry : ClassFinder
				.getStaticFieldValues(ConfigurationKeyBase.class,
						"ch.ethz.ruediste.roofline.measurementDriver")) {
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
