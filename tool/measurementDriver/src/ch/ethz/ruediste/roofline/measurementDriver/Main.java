package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;

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
		/*
		 * System.err.println(args.length); for (String arg : args) {
		 * System.err.println(arg); }
		 */

		if (args.length == 4 && "-autocomplete".equals(args[0])) {
			doAutocompetion(args);
			// exit
			return;
		}

		// parse command line
		List<String> parsedArgs = parseCommandLine(args);

		// load user configuration
		configuration.loadUserConfiguration();

		if (parsedArgs.size() < 1) {
			throw new Error("expected command name");
		}
		else {

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
			}

			if (command != null) {
				// execute the command
				parsedArgs.remove(0);
				command.execute(parsedArgs);
			}
		}
	}

	private void doAutocompetion(String[] args) {
		// get the partial word
		String partialWord = args[2];

		// System.err.println("partialWord: " + partialWord);

		if (partialWord.startsWith("-")) {
			// do the completion for a flag
			for (Pair<Class<?>, ConfigurationKeyBase> pair : ClassFinder
					.getStaticFieldValues(ConfigurationKeyBase.class,
							"ch.ethz.ruediste.roofline.measurementDriver")) {
				if (pair.getSecond().getKey()
						.startsWith(partialWord.substring(1))) {
					System.out.println("-" + pair.getSecond().getKey());
				}
			}
			// we're done
			return;
		}

		// get the commandline
		String commandLineString = System.getenv("COMP_LINE");
		// System.err.println("command line: " + commandLineString);

		CommandLine commandLine = CommandLine.parse(commandLineString);
		String[] completionArgs = commandLine.getArguments();

		// filter the completionArgs
		List<String> filteredCompletionArgs = new ArrayList<String>();
		for (String arg : completionArgs) {
			// remove all flags
			if (arg.startsWith("-")) {
				continue;
			}

			filteredCompletionArgs.add(arg);
		}

		/*
		 * System.err.println("filteredCompletionArgs: " +
		 * filteredCompletionArgs.size()); for (String arg :
		 * filteredCompletionArgs) { System.err.println(arg); }
		 */

		// is completion desired for the command?
		if (
		// the command line is empty
		filteredCompletionArgs.size() == 0

				// there is exactly one argument, and it is beeing edited
				|| (filteredCompletionArgs.size() == 1 && !partialWord
						.isEmpty())) {

			// propose all commands
			for (Class<? extends ICommand> clazz : instantiator
					.getBoundClasses(ICommand.class)) {
				ICommand cmd = instantiator.getInstance(clazz);
				if (partialWord.isEmpty()
						|| cmd.getName().startsWith(partialWord)) {
					System.out.println(cmd.getName());
				}
			}

			// we're done
			return;
		}

		// the command is set already, thus we have to ask the command for
		// completion
		if (filteredCompletionArgs.size() >= 1) {
			ICommand command = null;
			try {
				command = instantiator
						.getInstance(Key.get(ICommand.class,
								Names.named(filteredCompletionArgs.get(0))));
			} catch (ConfigurationException e) {
				// swallow
			}

			if (command != null && command instanceof IAutoCompletionCommand) {
				filteredCompletionArgs.remove(0);
				((IAutoCompletionCommand) command)
						.doAutoCompletion(partialWord, filteredCompletionArgs);
			}
		}
	}

	/**
	 * parse the command line, extract configuration options
	 */
	private List<String> parseCommandLine(String[] args) {
		ArrayList<String> unhandledArgs = new ArrayList<String>();

		for (String arg : args) {
			if (arg.startsWith("-")) {

				// it's a configuration
				String[] argParts = arg.substring(1).split("=");
				if (argParts.length != 2) {
					throw new Error(
							"Expected configuration definition in the format of -<key>=<value>, got "
									+ arg);
				}
				configuration.set(argParts[0], argParts[1]);
			}
			else {
				// it's a normal argument
				unhandledArgs.add(arg);
			}
		}

		// finally check if the configuration is still valid.
		configuration.checkConfiguration();

		return unhandledArgs;
	}

}
