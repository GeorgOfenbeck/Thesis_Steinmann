package ch.ethz.ruediste.roofline.measurementDriver.configuration;

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

	public static <T> ConfigurationKey<T> Create(Class<T> clazz, String key,
			String description, T defaultValue) {
		return new ConfigurationKey<T>(clazz, key, description, defaultValue);
	}
}
