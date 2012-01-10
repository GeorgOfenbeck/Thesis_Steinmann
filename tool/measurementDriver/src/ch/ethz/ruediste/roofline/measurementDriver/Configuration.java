package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.inject.Singleton;

@Singleton
public class Configuration {
	public static ConfigurationKey<String> userConfigFileKey = ConfigurationKey
			.Create(String.class, "userConfigFile",
					"location and filename of the user configuration file",
					"~/.roofline/config");

	CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
	MapConfiguration mapConfiguration = new MapConfiguration(
			new HashMap<String, Object>());

	private PropertiesConfiguration userConfiguration;

	public Configuration() {
		// add the map configuration with highest precedence
		combinedConfiguration.addConfiguration(mapConfiguration);

		userConfiguration = new PropertiesConfiguration();
		combinedConfiguration.addConfiguration(userConfiguration);

		// load the default configuration
		PropertiesConfiguration defaultConfiguration = new PropertiesConfiguration();
		try {
			InputStream configStream = ClassLoader
					.getSystemResourceAsStream("defaultConfiguration.config");
			if (configStream == null) {
				throw new Error(
						"could not load <defaultConfiguration.config>. Does not seem to be in the class path. Is it compiled into the .jar?");
			}
			defaultConfiguration.load(configStream);
		} catch (ConfigurationException e) {
			throw new Error(e);
		}

		combinedConfiguration.addConfiguration(defaultConfiguration);
	}

	/**
	 * Load the user configuration. This should be called after the command line
	 * options have been parsed to allow the location of the user configuration
	 * to be modified through command line arguments.
	 */
	public void loadUserConfiguration() {
		// retrieve the user configuration file
		String userConfigFileString = get(userConfigFileKey);

		// replace a starting tilde with the user home directory
		if (userConfigFileString.startsWith("~")) {
			userConfigFileString = System.getProperty("user.home")
					+ userConfigFileString.substring(1);
		}

		// check if the user configuration file exists
		File userConfigFile = new File(userConfigFileString);
		if (userConfigFile.exists()) {

			try {
				// load the user configuration file
				userConfiguration.load(userConfigFile);
			} catch (ConfigurationException e) {
				throw new Error(e);
			}

		}
	}

	/**
	 * check if all configuration properties found correspond to a configuration
	 * key
	 */
	public void checkConfiguration() throws Error {
		// check if all configuration properties found correspond to a
		// configuration key
		Iterator<?> it = combinedConfiguration.getKeys();

		Map<String, ConfigurationKeyBase> configurationKeyMap = getConfigurationKeyMap("ch.ethz.ruediste.roofline.measurementDriver");
		while (it.hasNext()) {
			Object key = it.next();
			if (!configurationKeyMap.containsKey(key)) {
				String availableKeys = StringUtils.join(
						configurationKeyMap.keySet(), "\n");
				throw new Error(
						String.format(
								"Key %s has been configured, but no corresponding ConfigurationKey has been declared. Declared Keys:\n%s",
								key, availableKeys));
			}
		}
	}

	public Object get(ConfigurationKeyBase key) {
		if (combinedConfiguration.containsKey(key.getKey())) {
			return combinedConfiguration.getProperty(key.getKey());
		}
		return key.getDefaultValue();
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ConfigurationKey<T> key) {
		if (Boolean.class.isAssignableFrom(key.getValueType())) {
			return (T) (Boolean) combinedConfiguration.getBoolean(key.getKey(),
					(Boolean) key.getDefaultValue());
		}
		if (Double.class.isAssignableFrom(key.getValueType())) {
			return (T) (Double) combinedConfiguration.getDouble(key.getKey(),
					(Double) key.getDefaultValue());
		}

		if (String.class.isAssignableFrom(key.getValueType())) {
			return (T) combinedConfiguration.getString(key.getKey(),
					(String) key.getDefaultValue());
		}

		if (Long.class.isAssignableFrom(key.getValueType())) {
			return (T) combinedConfiguration.getLong(key.getKey(),
					(Long) key.getDefaultValue());
		}

		throw new Error("Unsupported configuration type: "
				+ key.getValueType().getSimpleName());
	}

	public <T> void set(ConfigurationKey<T> key, T value) {
		mapConfiguration.clearProperty(key.getKey());
		mapConfiguration.addProperty(key.getKey(), value);
	}

	/**
	 * Construct a map for all configuration keys found in classes in the given
	 * package and subpackages. The key in the map is the key of the
	 * configuration key, the value is the configuration key object.
	 */
	public Map<String, ConfigurationKeyBase> getConfigurationKeyMap(
			String packageName) {
		HashMap<String, ConfigurationKeyBase> map = new HashMap<String, ConfigurationKeyBase>();
		for (Pair<Class<?>, ConfigurationKeyBase> pair : ClassFinder
				.getStaticFieldValues(
						ConfigurationKeyBase.class, packageName)) {
			map.put(pair.getRight().getKey(), pair.getRight());
		}
		return map;
	}

	public void set(String key, Object value) {
		mapConfiguration.setProperty(key, value);
	}
}
