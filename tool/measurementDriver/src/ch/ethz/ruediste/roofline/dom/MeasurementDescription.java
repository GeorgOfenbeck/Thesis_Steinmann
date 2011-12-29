package ch.ethz.ruediste.roofline.dom;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;

public class MeasurementDescription extends MeasurementDescriptionData {
	@Override
	public String toString() {
		return String.format("%s:%s:%s",
				toString(getKernel()),
				toString(getMeasurer()),
				toString(getScheme())

				);
	}

	private String toString(Object o) {
		if (o == null)
			return "null";

		String result = o.getClass().getSimpleName();
		if (result.endsWith("Description")) {
			result = result.substring(0,
					result.length() - "Description".length());
		}
		return result;
	}

	public void addMacro(MacroKey key, String definition) {
		PreprocessorMacro macro = new PreprocessorMacro();
		macro.setName(key.getMacroName());
		macro.setDefinition(definition);
		getMacros().add(macro);
	}

	/**
	 * return the definition of the macro identified by macroKey. If the macro
	 * was not defined in this measurement description, return the default
	 * definition.
	 */
	public String getMacroDefinition(MacroKey macroKey) {
		for (PreprocessorMacro m : getMacros()) {
			if (m.getName().equals(macroKey.getMacroName())) {
				return m.getDefinition();
			}
		}

		return macroKey.getDefaultValue();
	}
}
