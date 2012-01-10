package ch.ethz.ruediste.roofline.dom;

import java.util.ArrayList;
import java.util.List;

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

	/**
	 * return the definition of the macro identified by key. If the macro was
	 * not defined in this measurement description, return the default
	 * definition.
	 */
	public String getMacroDefinition(MacroKey key) {
		List<String> availableDefinitions = new ArrayList<String>();

		// add the macro definition if it is defined on the measurement
		// description
		if (isMacroDefined(key)) {
			availableDefinitions.add(super.getMacroDefinition(key));
		}

		// check the kernel for a definition
		if (getKernel() != null && getKernel().isMacroDefined(key)) {
			availableDefinitions.add(getKernel().getMacroDefinition(key));
		}

		// check the measureer for a definition
		if (getMeasurer() != null && getMeasurer().isMacroDefined(key)) {
			availableDefinitions.add(getMeasurer().getMacroDefinition(key));
		}

		// check the measurement scheme for a definition
		if (getScheme() != null && getScheme().isMacroDefined(key)) {
			availableDefinitions.add(getScheme().getMacroDefinition(key));
		}

		// return the default value if no definition has been found
		if (availableDefinitions.size() == 0) {
			return key.getDefaultValue();
		}

		// at least one definition was given. Extract the first one
		String firstDefinition = availableDefinitions.get(0);

		// check if all definitions are equal to the first definition
		for (String definition : availableDefinitions) {
			if (!firstDefinition.equals(definition)) {
				throw new Error(
						String.format(
								"found multiple definitions for macro %s: <%s> and <%s>",
								key.getMacroName(), firstDefinition, definition));
			}
		}

		// since all definitions are equal, it does not matter which one is
		// returned
		return firstDefinition;
	}
}
