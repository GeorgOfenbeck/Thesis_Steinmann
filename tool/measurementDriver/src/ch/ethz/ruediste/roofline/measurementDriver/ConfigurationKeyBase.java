package ch.ethz.ruediste.roofline.measurementDriver;

public abstract class ConfigurationKeyBase {

	protected final String key;
	protected final String description;

	public ConfigurationKeyBase(String key, String description) {
		this.key = key;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}

	public abstract Object getDefaultValue();

	public abstract Class<?> getValueType();
}