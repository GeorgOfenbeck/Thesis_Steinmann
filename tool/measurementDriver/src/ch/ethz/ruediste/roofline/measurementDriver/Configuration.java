package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Configuration {
	CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
	MapConfiguration mapConfiguration = new MapConfiguration(
			new HashMap<String, Object>());

	public Configuration() {
		// add the map configuration with highest precedence
		combinedConfiguration.addConfiguration(mapConfiguration);

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

	@SuppressWarnings("unchecked")
	public <T> T get(ConfigurationKey<T> key) {
		if (Boolean.class.isAssignableFrom(key.getClazz())) {
			return (T) (Boolean) combinedConfiguration.getBoolean(key.getKey(),
					(Boolean) key.getDefaultValue());
		}
		if (Double.class.isAssignableFrom(key.getClazz())) {
			return (T) (Double) combinedConfiguration.getDouble(key.getKey(),
					(Double) key.getDefaultValue());
		}

		if (String.class.isAssignableFrom(key.getClazz())) {
			return (T) combinedConfiguration.getString(key.getKey(),
					(String) key.getDefaultValue());
		}

		if (Long.class.isAssignableFrom(key.getClazz())) {
			return (T) combinedConfiguration.getLong(key.getKey(),
					(Long) key.getDefaultValue());
		}

		throw new Error("Unsupported configuration type: "
				+ key.getClazz().getSimpleName());
	}

	public <T> void set(ConfigurationKey<T> key, T value) {
		mapConfiguration.clearProperty(key.getKey());
		mapConfiguration.addProperty(key.getKey(), value);
	}
}
