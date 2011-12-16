package ch.ethz.ruediste.roofline.measurementDriver;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;

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
		for (Pair<Class<?>, ConfigurationKeyBase> pair : getConfigurationKeys(packageName)) {
			map.put(pair.getSecond().getKey(), pair.getSecond());
		}
		return map;
	}

	/**
	 * Finds all configuration keys declared in the given package and it's sub
	 * packages, along with the class the key is defined in
	 * 
	 * @param packageName
	 *            TODO
	 */
	public List<Pair<Class<?>, ConfigurationKeyBase>> getConfigurationKeys(
			String packageName) {
		List<Pair<Class<?>, ConfigurationKeyBase>> configurationKeys = new ArrayList<Pair<Class<?>, ConfigurationKeyBase>>();

		// get all classes in the package
		List<Class<?>> classes = ClassFinder.getClasses(packageName);

		// loop over all classes
		for (Class<?> clazz : classes) {
			// loop over the fields in the class
			for (Field field : clazz.getDeclaredFields()) {
				// check if the field is static and a ConfigurationKey
				if (Modifier.isStatic(field.getModifiers())
						&& ConfigurationKeyBase.class.isAssignableFrom(field
								.getType()))
				{
					// the field contains a configuration key
					try {
						// retrieve the configuration key object
						ConfigurationKeyBase key = (ConfigurationKeyBase) field
								.get(null);

						// add the configuration key to the result list
						configurationKeys
								.add(new Pair<Class<?>, ConfigurationKeyBase>(
										clazz, key));
					} catch (IllegalArgumentException e) {
						// ignore
					} catch (IllegalAccessException e) {
						// ignore
					}
				}
			}
		}
		return configurationKeys;
	}

	public void set(String key, Object value) {
		mapConfiguration.setProperty(key, value);
	}
}
