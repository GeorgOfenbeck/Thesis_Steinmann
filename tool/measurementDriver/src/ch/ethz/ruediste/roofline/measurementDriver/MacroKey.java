package ch.ethz.ruediste.roofline.measurementDriver;

public class MacroKey {
	private final String defaultValue;
	private final String macroName;
	private final String description;

	private MacroKey(String macroName, String description,
			String defaultValue) {
		this.macroName = macroName;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getMacroName() {
		return macroName;
	}

	public static MacroKey Create(
			String key, String description, String defaultValue) {
		return new MacroKey(key, description, defaultValue);
	}
}
