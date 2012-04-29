package ch.ethz.ruediste.roofline.measurementDriver.commandControllers;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.*;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.repositories.ReflectionRepository;
import ch.ethz.ruediste.roofline.measurementDriver.util.Instantiator;

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

	@Inject
	public ReflectionRepository reflectionRepository;

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
		}
		catch (Exception e) {
			throw new Error(e);
		}

		System.out.println("\nCommands:");
		System.out.println("*********");

		instantiator.listNamed(ICommandController.class);

		System.out.println("\nMeasurement Controllers:");
		System.out.println("*************");
		instantiator.listNamed(IMeasurementController.class);

		System.out.println("\nConfiguration Flags:");
		System.out.println("**********************");

		for (Pair<Class<?>, ConfigurationKeyBase> entry : reflectionRepository
				.getConfigurationKeyPairs()) {
			System.out.printf("%s = %s\n\t(%s / %s)\n\t%s\n\n",
					// key
					entry.getRight().getKey(),
					// current value
					configuration.getUntyped(entry.getRight()),
					// declaring class
					entry.getLeft().getSimpleName(),
					// type of the value
					entry.getRight().getValueType().getSimpleName(),
					// description
					entry.getRight().getDescription().replace("\n", "\t\n"));
		}
	}
}
