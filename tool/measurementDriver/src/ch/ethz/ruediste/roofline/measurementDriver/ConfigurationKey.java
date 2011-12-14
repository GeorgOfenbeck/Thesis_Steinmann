package ch.ethz.ruediste.roofline.measurementDriver;

public class ConfigurationKey<T> {
	private final Class<T> clazz;
	private final String key;
	private final String description;
	private final T defaultValue;

	private ConfigurationKey(Class<T> clazz, String key, String description,
			T defaultValue) {
		this.clazz = clazz;
		this.key = key;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public static <T> ConfigurationKey<T> Create(Class<T> clazz,
			String key, String description, T defaultValue) {
		return new ConfigurationKey<T>(clazz, key, description, defaultValue);
	}
}
