package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.IOException;
import java.util.*;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.ICommandController;
import ch.ethz.ruediste.roofline.measurementDriver.commandControllers.IAutoCompletionAwareCommandController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.repositories.ReflectionRepository;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;

import com.google.inject.*;
import com.google.inject.name.Names;

public class Main {
	public static ConfigurationKey<Boolean> debugOutputKey = ConfigurationKey
			.Create(Boolean.class,
					"debug",
					"if set to true, the debug log statements are printed to the console",
					false);

	public static ConfigurationKey<String> logLevelKey = ConfigurationKey
			.Create(String.class, "logLevel",
					"logLevel to be used for console logging", "INFO");

	public static ConfigurationKey<String> userConfigFileKey = ConfigurationKey
			.Create(String.class, "userConfigFile",
					"location and filename of the user configuration file",
					"~/.roofline/config");

	public static ConfigurationKey<String> userLog4jConfigFileKey = ConfigurationKey
			.Create(String.class,
					"userLog4jConfig",
					"location and filename of the user log4j configuration file",
					"~/.roofline/log4j.config");

	static private Logger log = Logger.getLogger(Main.class);

	@Inject
	public Instantiator instantiator;

	@Inject
	public Configuration configuration;

	@Inject
	public RuntimeMonitor runtimeMonitor;

	@Inject
	public ReflectionRepository reflectionRepository;

	@Inject
	public MainHelper mainHelper;

	public static void main(String args[]) throws IOException {
		Injector injector = MainHelper.createInjector();

		Main main = injector.getInstance(Main.class);

		main.mainInst(args);

	}

	/**
	 * The main method as instance method, which can use dependency injection
	 */
	private void mainInst(String args[]) throws IOException {
		Instantiator.instance = instantiator;

		/*
		 * System.err.println(args.length); for (String arg : args) {
		 * System.err.println(arg); }
		 */

		if (args.length == 4 && "-autocomplete".equals(args[0])) {
			doAutocompetion(args);
			// exit
			return;
		}

		runtimeMonitor.rootCategory.enter();
		runtimeMonitor.startupCategory.enter();

		// create configurations
		Configuration defaultConfiguration = new Configuration();
		Configuration userConfiguration = new Configuration();

		// wire configurations
		configuration.setParentConfiguration(userConfiguration);
		userConfiguration.setParentConfiguration(defaultConfiguration);

		// load default configuration
		mainHelper.loadDefaultConfiguration(defaultConfiguration);

		// parse command line
		List<String> parsedArgs = parseCommandLine(args);

		// load user Configuration
		mainHelper.loadUserConfiguration(userConfiguration);

		// configure log4j
		mainHelper.configureLog4j();

		runtimeMonitor.startupCategory.leave();

		if (parsedArgs.size() < 1) {
			throw new Error("expected command name");
		}
		else {

			String commandName = parsedArgs.get(0);

			// get the command
			ICommandController command = null;
			try {
				command = instantiator.getInstance(Key.get(
						ICommandController.class, Names.named(commandName)));
			}
			catch (ConfigurationException e) {
				log.fatal(String
						.format("Could not find the command named %s\nAvailable Commands:\n",
								commandName));
				instantiator.listNamed(ICommandController.class);
			}

			if (command != null) {
				// execute the command
				parsedArgs.remove(0);
				command.execute(parsedArgs);
			}
		}
		runtimeMonitor.rootCategory.leave();
		runtimeMonitor.print();
	}

	private void doAutocompetion(String[] args) {
		// get the partial word
		String partialWord = args[2];

		// System.err.println("partialWord: " + partialWord);

		if (partialWord.startsWith("-")) {
			// do the completion for a flag
			for (Pair<Class<?>, ConfigurationKeyBase> pair : reflectionRepository
					.getConfigurationKeyPairs()) {
				if (pair.getRight().getKey()
						.startsWith(partialWord.substring(1))) {
					System.out.println("-" + pair.getRight().getKey());
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
			for (Class<? extends ICommandController> clazz : instantiator
					.getBoundClasses(ICommandController.class)) {
				ICommandController cmd = instantiator.getInstance(clazz);
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
			ICommandController command = null;
			try {
				command = instantiator.getInstance(Key.get(
						ICommandController.class,
						Names.named(filteredCompletionArgs.get(0))));
			}
			catch (ConfigurationException e) {
				// swallow
			}

			if (command != null
					&& command instanceof IAutoCompletionAwareCommandController) {
				filteredCompletionArgs.remove(0);
				((IAutoCompletionAwareCommandController) command)
						.doAutoCompletion(partialWord, filteredCompletionArgs);
			}
		}
	}

	/**
	 * parse the command line, extract configuration options
	 */
	private List<String> parseCommandLine(String[] args) {
		ArrayList<String> unhandledArgs = new ArrayList<String>();

		for (String rawArg : args) {
			// remove starting and trailing double quotes
			String arg = rawArg;
			if (arg.startsWith("\""))
				arg = arg.substring(1);
			if (arg.endsWith("\""))
				arg = arg.substring(0, arg.length() - 1);

			if (arg.startsWith("-")) {
				// it's a configuration
				String[] argParts = arg.substring(1).split("=");

				// find the key for the argument
				Map<String, ConfigurationKeyBase> configurationKeyMap = reflectionRepository
						.getConfigurationKeyMap();
				ConfigurationKeyBase key = configurationKeyMap.get(argParts[0]);

				if (key == null) {
					throw new Error(
							String.format(
									"could not find configuration key named %s. Available keys:\n%s",
									arg.substring(1),
									StringUtils.join(
											configurationKeyMap.keySet(), "\n")));
				}

				// is it a boolean which should be toggled?
				if (argParts.length == 1) {

					// check if key is a boolean
					if (!Boolean.class.isAssignableFrom(key.getValueType())) {
						throw new Error(
								String.format(
										"key %s of type %s is not a boolean, which could be toggled",
										key.getKey(), key.getValueType()
												.getName()));
					}

					@SuppressWarnings("unchecked")
					ConfigurationKey<Boolean> boolKey = (ConfigurationKey<Boolean>) key;

					// System.out.println("toggling " + boolKey.getKey());
					// toggle the boolean
					configuration.set(boolKey, !configuration.get(boolKey));
				}
				else {
					// it's a normal configuration
					if (argParts.length != 2) {
						throw new Error(
								"Expected configuration definition in the format of -<key>=<value>, got "
										+ arg);
					}

					configuration.parseAndSet(key, argParts[1]);
				}
			}
			else {
				// it's a normal argument
				unhandledArgs.add(arg);
			}
		}

		return unhandledArgs;
	}

}
