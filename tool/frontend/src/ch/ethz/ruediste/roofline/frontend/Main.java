package ch.ethz.ruediste.roofline.frontend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

public class Main {
	private static final String toolPathKey = "toolPath";
	private static final String buildKey = "doBuild";
	private static final String useDaemonKey = "useDaemon";
	private static final String showBuildOutputKey = "showBuildOutput";
	private static final String cleanKey = "clean";

	private static CombinedConfiguration configuration;

	public static void main(String args[]) throws ExecuteException, IOException {
		System.out.println("Roofline Measuring Tool");

		// load the configuration
		int parameterNumber = setupConfiguration(args);

		// check if build is required
		if (configuration.getBoolean(buildKey)) {
			System.out.println("building tool");
			build();
		}

		System.out.println("starting tool");

		execute(args, parameterNumber);
	}

	private static int setupConfiguration(String args[]) {

		configuration = new CombinedConfiguration();

		int parameterNumber;
		Map<String, String> map = new HashMap<String, String>();
		// parse command line
		for (parameterNumber = 0; parameterNumber < args.length; parameterNumber++) {
			if (args[parameterNumber].equals("-nb")) {
				map.put(buildKey, "false");
				System.out.println("parsed parameter -nb");
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

			break;
		}
		configuration.addConfiguration(new MapConfiguration(map));

		// load default configuration compiled into jar
		PropertiesConfiguration defaultConfiguration = new PropertiesConfiguration();
		try {
			InputStream configStream = ClassLoader
					.getSystemResourceAsStream("defaultConfiguration.config");
			defaultConfiguration.load(configStream);
		} catch (ConfigurationException e) {
			throw new Error(e);
		}
		configuration.addConfiguration(defaultConfiguration);

		return parameterNumber;
	}

	private static void execute(String[] args, int parameterNumber)
			throws ExecuteException, IOException {
		// setup command line
		CommandLine cmdLine = new CommandLine("java");
		cmdLine.addArgument("-jar");

		File sourceDir = new File(configuration.getString(toolPathKey));
		File jar = new File(sourceDir,
				"measurementDriver/build/distributions/measurementDriver.jar");

		cmdLine.addArgument(jar.getAbsolutePath());

		for (int i = parameterNumber; i < args.length; i++) {
			cmdLine.addArgument(args[i]);
		}

		System.out.println(cmdLine);

		// setup executor
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(0);

		// perform command
		try {
			executor.execute(cmdLine);
		} catch (ExecuteException e) {
			System.err.println(e.getMessage());
			System.exit(e.getExitValue());
		}
	}

	private static void build() throws ExecuteException, IOException {
		// setup command line
		CommandLine cmdLine = new CommandLine("./gradlew");

		if (configuration.getBoolean(useDaemonKey)) {
			cmdLine.addArgument("--daemon");
		}

		if (configuration.getBoolean(cleanKey)) {
			cmdLine.addArgument(":measurementDriver:clean");
			cmdLine.addArgument(":multiLanguageCodeGenerator:clean");
		}
		cmdLine.addArgument(":measurementDriver:build");

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
