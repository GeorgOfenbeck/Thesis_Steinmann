package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommand;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class Main {

	@Inject
	public Instantiator instantiator;

	@Inject
	public Configuration configuration;

	public static void main(String args[]) throws IOException {
		// setup dependency Injection
		Injector injector = Guice.createInjector(new MainModule());

		// make the injector available to the instantiator
		injector.getInstance(Instantiator.class).setInjector(injector);

		Main main = injector.getInstance(Main.class);
		main.mainInst(args);

	}

	/**
	 * The main method as instance method, which can use dependency injection
	 */
	private void mainInst(String args[]) throws IOException {

		// parse command line
		List<String> parsedArgs = parseCommandLine(args);

		if (parsedArgs.size() < 1) {
			throw new Error("expected command name");
		}

		String commandName = parsedArgs.get(0);

		// get the command
		ICommand command = null;
		try {
			command = instantiator
					.getInstance(Key.get(ICommand.class,
							Names.named(commandName)));
		} catch (ConfigurationException e) {
			System.out
					.printf("Could not find the command named %s\nAvailable Commands:\n",
							commandName);
			instantiator.listNamed(ICommand.class);
			System.exit(1);
		}

		// execute the command
		command.execute(parsedArgs);
	}

	/**
	 * parse the command line, extract configuration options
	 */
	private List<String> parseCommandLine(String[] args) {
		ArrayList<String> parsedArgs = new ArrayList<String>();

		for (String arg : args) {
			if (arg.startsWith("-")) {
				// it's a configuration
				String[] argParts = arg.substring(1).split("=");
				if (argParts.length != 2) {
					throw new Error(
							"Expected configuration definition in the format of <key>=<value>");
				}
				configuration.set(argParts[0], argParts[1]);
			}
			else {
				// it's a normal argument
				parsedArgs.add(arg);
			}
		}

		// finally check if the configuration is still valid.
		configuration.checkConfiguration();

		return parsedArgs;
	}

}
