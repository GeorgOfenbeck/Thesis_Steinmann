package ch.ethz.ruediste.roofline.measurementDriver.configuration;

/**
 * base class for configuration keys. Not parameterized on the type of the value
 */
public abstract class ConfigurationKeyBase {

	protected final String key;
	protected final String description;

	public ConfigurationKeyBase(String key, String description) {
		this.key = key;
		this.description = description;
	}

	/**
	 * get the string identifying the configuration option
	 */
	public String getKey() {
		return key;
	}

	/**
	 * get the description of the configuration key
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * get the default value
	 */
	public abstract Object getDefaultValue();

	/**
	 * get the type of the value
	 */
	public abstract Class<?> getValueType();
}