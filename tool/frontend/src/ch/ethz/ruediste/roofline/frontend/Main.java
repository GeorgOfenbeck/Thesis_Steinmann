package ch.ethz.ruediste.roofline.frontend;

import java.io.*;
import java.util.*;

import org.apache.commons.configuration.*;
import org.apache.commons.exec.*;

public class Main {
	/*
	 * Configuration keys
	 */

	/**
	 * class path to be used to start the measurement driver
	 */
	private static final String classPathKey = "classPath";

	/**
	 * path of the tool, used for building
	 */
	private static final String toolPathKey = "toolPath";

	/**
	 * flag indicating if the measurement driver should be built before starting
	 * it
	 */
	private static final String buildKey = "doBuild";

	/**
	 * flag indicating if the gradle build daemon should be used
	 */
	private static final String useDaemonKey = "useDaemon";

	/**
	 * flag indicating if the output of the build process should be shown on the
	 * console
	 */
	private static final String showBuildOutputKey = "showBuildOutput";

	/**
	 * flag indicating if the measurement driver should be cleaned before
	 * building it
	 */
	private static final String cleanKey = "clean";

	/**
	 * flag indicating if the gradle daemon should be restarted
	 */
	private static final String restartGradleDaemonKey = "restartGradleDaemon";

	/**
	 * location of the user configuration file
	 */
	private static final String userConfigFileKey = "userConfigFile";

	private static CombinedConfiguration configuration;
	private static boolean showHelp = false;

	public static void main(String args[]) throws ExecuteException, IOException {
		// if autocomplete is desired, just call the currently compiled
		// measurement driver
		if (args.length == 4 && "-autocomplete".equals(args[0])) {
			// setup the configuration, without arguments
			setupConfiguration(new String[] {});

			// copy the arguments into a List
			List<String> argList = new ArrayList<String>();
			Collections.addAll(argList, args);

			// execute the measurement Driver
			startMeasurementDriver(argList);

			// exit
			return;
		}

		System.out.println("Roofline Measuring Tool");

		// load the configuration
		List<String> unhandledParameters = setupConfiguration(args);

		// display help if desired
		if (args.length == 0 || showHelp) {
			// copy help.txt, which is included in the jar, to the standard
			// output
			InputStream helpTextStream = ClassLoader
					.getSystemResourceAsStream("help.txt");
			int ch;
			while ((ch = helpTextStream.read()) > 0) {
				System.out.write(ch);
			}
			helpTextStream.close();
			System.exit(0);
		}

		// restart gradle daemon if desired
		if (configuration.getBoolean(restartGradleDaemonKey)) {
			System.out.println("restarting gradle daemon");
			restartGradleDaemon();
		}
		// check if build is required
		if (configuration.getBoolean(buildKey)) {
			System.out.println("building tool");
			build();
		}

		System.out.println("starting tool");

		startMeasurementDriver(unhandledParameters);
	}

	/**
	 * parse the command line and setup the configuration
	 * 
	 */
	private static List<String> setupConfiguration(String args[]) {
		List<String> unhandledParameters = new ArrayList<String>();

		Map<String, String> map = new HashMap<String, String>();
		// parse command line
		for (int parameterNumber = 0; parameterNumber < args.length; parameterNumber++) {
			if (args[parameterNumber].equals("-nb")) {
				map.put(buildKey, "false");
				continue;
			}
			if (args[parameterNumber].equals("-b")) {
				map.put(buildKey, "true");
				continue;
			}
			if (args[parameterNumber].equals("-nd")) {
				map.put(useDaemonKey, "false");
				continue;
			}
			if (args[parameterNumber].equals("-d")) {
				map.put(useDaemonKey, "true");
				continue;
			}
			if (args[parameterNumber].equals("-c")) {
				map.put(cleanKey, "true");
				continue;
			}

			if (args[parameterNumber].equals("-nr")) {
				map.put(restartGradleDaemonKey, "false");
				continue;
			}

			if (args[parameterNumber].equals("-r")) {
				map.put(restartGradleDaemonKey, "true");
				continue;
			}

			if (args[parameterNumber].equals("-h")) {
				showHelp = true;
				continue;
			}

			if (args[parameterNumber].startsWith("-ucf")) {
				if (!args[parameterNumber].startsWith("-ucf=")) {
					throw new Error("expected -ucf=<user config file>");
				}
				map.put(userConfigFileKey,
						args[parameterNumber].substring("-ucf=".length()));
				continue;
			}

			// the parameter has not been hadled, add it to the unhandled
			// parameters
			unhandledParameters.add(args[parameterNumber]);
		}

		// load default configuration compiled into jar
		PropertiesConfiguration defaultConfiguration = new PropertiesConfiguration();
		try {
			InputStream configStream = ClassLoader
					.getSystemResourceAsStream("defaultConfiguration.config");
			defaultConfiguration.load(configStream);
		}
		catch (ConfigurationException e) {
			throw new Error(e);
		}

		// load user configuration
		PropertiesConfiguration userConfiguration = new PropertiesConfiguration();

		// get the file name of the user configuration from the configuration
		String userConfigFileName = null;
		if (defaultConfiguration.containsKey(userConfigFileKey))
			userConfigFileName = defaultConfiguration
					.getString(userConfigFileKey);
		if (map.containsKey(userConfigFileKey))
			userConfigFileName = map.get(userConfigFileKey);
		if (userConfigFileName != null) {
			// replace a starting tilde with the user home directory
			if (userConfigFileName.startsWith("~")) {
				userConfigFileName = System.getProperty("user.home")
						+ userConfigFileName.substring(1);
			}

			// check if the user configuration file exists
			File userConfigFile = new File(userConfigFileName);
			if (userConfigFile.exists() && userConfigFile.isFile()) {
				try {
					// load the user configuration file
					userConfiguration.load(userConfigFile);
				}
				catch (ConfigurationException e) {
					throw new Error(e);
				}

			}
		}

		// setup the combined configuration
		configuration = new CombinedConfiguration();

		configuration.addConfiguration(new MapConfiguration(map));
		configuration.addConfiguration(userConfiguration);
		configuration.addConfiguration(defaultConfiguration);

		return unhandledParameters;
	}

	/**
	 * start the measuremend driver
	 */
	private static void startMeasurementDriver(List<String> args)
			throws ExecuteException, IOException {
		// setup command line
		CommandLine cmdLine = new CommandLine("java");
		cmdLine.addArgument("-cp");
		cmdLine.addArgument(configuration.getString(classPathKey));
		cmdLine.addArgument("ch.ethz.ruediste.roofline.measurementDriver.Main");

		for (String arg : args) {
			cmdLine.addArgument(arg);
		}

		// System.out.println(cmdLine);

		// setup executor
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(0);

		// perform command
		try {
			executor.execute(cmdLine);
		}
		catch (ExecuteException e) {
			System.err.println(e.getMessage());
			System.exit(e.getExitValue());
		}
	}

	/**
	 * restart the gradle daemon
	 */
	private static void restartGradleDaemon() throws ExecuteException,
			IOException {
		// setup command line
		CommandLine cmdLine = new CommandLine("./gradlew");

		cmdLine.addArgument("--daemon");

		// setup executor
		DefaultExecutor executor = new DefaultExecutor();

		executor.setExitValue(0);
		executor.setWorkingDirectory(new File(configuration
				.getString(toolPathKey)));

		// perform command
		executor.execute(cmdLine);
	}

	/**
	 * build the measurement driver
	 */
	private static void build() throws ExecuteException, IOException {
		// setup command line
		CommandLine cmdLine = new CommandLine("./gradlew");

		if (configuration.getBoolean(restartGradleDaemonKey)) {
			cmdLine.addArgument("--stop");
		}

		if (configuration.getBoolean(useDaemonKey)) {
			cmdLine.addArgument("--daemon");
		}

		if (configuration.getBoolean(cleanKey)) {
			cmdLine.addArgument(":measurementDriver:clean");
			cmdLine.addArgument(":sharedEntityGenerator:clean");
		}
		cmdLine.addArgument(":measurementDriver:classes");

		// setup executor
		DefaultExecutor executor = new DefaultExecutor();

		// set the output to null if the output should not be shown
		// still show the errors
		if (!configuration.getBoolean(showBuildOutputKey)) {
			executor.setStreamHandler(new PumpStreamHandler(null, System.err));
		}

		executor.setExitValue(0);
		executor.setWorkingDirectory(new File(configuration
				.getString(toolPathKey)));

		// perform command
		executor.execute(cmdLine);
	}
}
