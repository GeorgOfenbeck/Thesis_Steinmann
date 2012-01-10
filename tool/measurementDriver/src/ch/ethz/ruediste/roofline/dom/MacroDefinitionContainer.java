package ch.ethz.ruediste.roofline.dom;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;

/**
 * Base class containing macro definitions. They are collected from the objects
 * making up the measurement description.
 */
public class MacroDefinitionContainer extends AttachedPropertyContainer {
	/**
	 * map containing the macro definition of this object
	 */
	private final Map<MacroKey, String> macroDefinitions = new HashMap<MacroKey, String>();

	/**
	 * return the macro definition map
	 */
	public Map<MacroKey, String> getMacroDefinitions() {
		return macroDefinitions;
	}

	/**
	 * return the definition of the specified macro
	 */
	protected String getMacroDefinition(MacroKey key) {
		if (macroDefinitions.containsKey(key)) {
			return macroDefinitions.get(key);
		}
		return key.getDefaultValue();
	}

	/**
	 * return true if the macro identified by key is defined within this object
	 */
	protected boolean isMacroDefined(MacroKey key) {
		return macroDefinitions.containsKey(key);
	}

	/**
	 * set the definition for a macro
	 */
	protected void setMacroDefinition(MacroKey key, String definition) {
		macroDefinitions.put(key, definition);
	}
}
