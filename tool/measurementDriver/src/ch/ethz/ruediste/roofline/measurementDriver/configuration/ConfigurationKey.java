package ch.ethz.ruediste.roofline.measurementDriver.configuration;

/**
 * configuration key
 * 
 * @param <T>
 *            type of the configuration value
 */
public class ConfigurationKey<T> extends ConfigurationKeyBase {
	private final Class<T> valueType;
	private final T defaultValue;

	private ConfigurationKey(Class<T> valueType, String key,
			String description, T defaultValue) {
		super(key, description);
		this.valueType = valueType;
		this.defaultValue = defaultValue;
	}

	public Class<T> getValueType() {
		return valueType;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * create a new configuration key
	 * 
	 * @param clazz
	 *            type of the value
	 * @param key
	 *            string key to identify the configuration
	 * @param description
	 *            description of the configuration option
	 * @param defaultValue
	 *            default value
	 * @return new configuration key
	 */
	public static <T> ConfigurationKey<T> Create(Class<T> clazz, String key,
			String description, T defaultValue) {
		return new ConfigurationKey<T>(clazz, key, description, defaultValue);
	}
}
