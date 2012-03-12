package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.*;
import java.util.Properties;

import org.apache.log4j.*;

import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.repositories.ReflectionRepository;
import ch.ethz.ruediste.roofline.measurementDriver.util.Instantiator;

import com.google.inject.*;

public class MainHelper {

	@Inject
	public ReflectionRepository reflectionRepository;

	@Inject
	public Configuration configuration;

	/**
	 * Create an injector and register it in the Instantiator
	 */
	public static Injector createInjector() {
		// setup dependency Injection
		Injector injector = Guice.createInjector(new MainModule());

		// make the injector available to the instantiator
		injector.getInstance(Instantiator.class).setInjector(injector);
		return injector;
	}

	/**
	 * parse the configuration file accessible through the inStream, and put all
	 * flags into the provided configuration.
	 */
	public void fillConfiguration(Configuration configuration,
			InputStream inStream) throws IOException {
		// load the stream
		Properties properties = new Properties();
		properties.load(inStream);

		// put properties into the configuration
		for (String key : properties.stringPropertyNames()) {
			ConfigurationKeyBase configKey = reflectionRepository
					.getConfigurationKeyMap().get(key);

			configuration.parseAndSet(configKey, properties.getProperty(key));
		}
	}

	/**
	 * Loads the user configuration into the supplied configuration. The
	 * configuration to be filled is passed into the method to allow the caller
	 * to wire up the configuration before calling this method.
	 */
	public void loadUserConfiguration(Configuration userConfiguration)
			throws FileNotFoundException, IOException {
		// load user configuration
		{
			// retrieve the user configuration file
			String userConfigFileString = configuration
					.get(Main.userConfigFileKey);

			// replace a starting tilde with the user home directory
			if (userConfigFileString.startsWith("~")) {
				userConfigFileString = System.getProperty("user.home")
						+ userConfigFileString.substring(1);
			}

			// check if the user configuration file exists
			File userConfigFile = new File(userConfigFileString);
			if (userConfigFile.exists() && userConfigFile.isFile()) {
				// parse the user configuration
				InputStream inStream = new FileInputStream(userConfigFile);
				fillConfiguration(userConfiguration, inStream);
			}
		}
	}

	/**
	 * @throws Error
	 * @param main
	 *            TODO
	 * @throws IOException
	 */
	public void configureLog4j() throws Error, IOException {
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
					configuration.get(Main.userLog4jConfigFileKey));
			if (userConfigFile.exists() && !userConfigFile.isDirectory()) {
				PropertyConfigurator
						.configure(userConfigFile.getAbsolutePath());
			}
		}

		// set the loglevel
		Logger.getRootLogger().setLevel(
				Level.toLevel(configuration.get(Main.logLevelKey)));

		// set debug loglevel
		if (configuration.get(Main.debugOutputKey)) {
			// System.out.println("set loglevel to debug");
			Logger.getRootLogger().setLevel((Level) Level.DEBUG);
		}
	}

	/**
	 * loads the default configuration stored in the JAR. The configuration is
	 * passed as argument to allow wiring it up before calling this method.
	 */
	public void loadDefaultConfiguration(Configuration defaultConfiguration)
			throws Error, IOException {
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
	}
}
