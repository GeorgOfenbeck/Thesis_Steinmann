package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.*;
import java.util.*;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.*;

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

		runtimeMonitor.rootCategory.enter();
		runtimeMonitor.startupCategory.enter();

		// create configurations
		Configuration defaultConfiguration = new Configuration();
		Configuration userConfiguration = new Configuration();

		// wire configurations
		configuration.setDefaultConfiguration(userConfiguration);
		userConfiguration.setDefaultConfiguration(defaultConfiguration);

		// load default configuration
		{
			InputStream inStream = ClassLoader
					.getSystemResourceAsStream("defaultConfiguration.config");
			if (inStream == null) {
				throw new Error(
						"could not load <defaultConfiguration.config>. Does not seem to be in the class path. Is it compiled into the .jar?");
			}

			fillConfiguration(defaultConfiguration, inStream);
		}

		// parse command line
		List<String> parsedArgs = parseCommandLine(args);

		// load user configuration
		{
			// retrieve the user configuration file

			String userConfigFileString = configuration.get(userConfigFileKey);

			// replace a starting tilde with the user home directory
			if (userConfigFileString.startsWith("~")) {
				userConfigFileString = System.getProperty("user.home")
						+ userConfigFileString.substring(1);
			}

			// check if the user configuration file exists
			File userConfigFile = new File(userConfigFileString);
			if (userConfigFile.exists() && userConfigFile.isFile()) {

				InputStream inStream = new FileInputStream(userConfigFile);
				fillConfiguration(userConfiguration, inStream);
			}
		}

		// initialize log4j
		{
			InputStream inStream = ClassLoader
					.getSystemResourceAsStream("log4j.config");
			if (inStream == null) {
				throw new Error(
						"could not load <log4j.config>. Does not seem to be in the class path. Is it compiled into the .jar?");
			}

			Properties properties = new Properties();
			properties.load(inStream);
			PropertyConfigurator.configure(properties);

			// load user configuration
			File userConfigFile = new File(
					configuration.get(userLog4jConfigFileKey));
			if (userConfigFile.exists() && !userConfigFile.isDirectory()) {
				PropertyConfigurator
						.configure(userConfigFile.getAbsolutePath());
			}
		}

		// set the loglevel
		Logger.getRootLogger().setLevel(
				Level.toLevel(configuration.get(logLevelKey)));

		// set debug loglevel
		if (configuration.get(debugOutputKey)) {
			// System.out.println("set loglevel to debug");
			Logger.getRootLogger().setLevel((Level) Level.DEBUG);
		}

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

	/**
	 * @param defauConfiguration
	 * @param inStream
	 * @throws IOException
	 */
	public void fillConfiguration(Configuration defauConfiguration,
			InputStream inStream) throws IOException {
		// load the stream
		Properties defaultProperties = new Properties();
		defaultProperties.load(inStream);

		// put properties into the configuration
		for (String key : defaultProperties.stringPropertyNames()) {
			ConfigurationKeyBase configKey = reflectionRepository
					.getConfigurationKeyMap().get(key);

			defauConfiguration.parseAndSet(configKey,
					defaultProperties.getProperty(key));
		}
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
