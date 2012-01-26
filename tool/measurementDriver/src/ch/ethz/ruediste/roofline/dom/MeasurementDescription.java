package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.dom.Axes.*;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class MeasurementDescription extends MeasurementDescriptionData {

	@XStreamOmitField
	private ValidationData validationData;

	public MeasurementDescription() {
	}

	public MeasurementDescription(Coordinate coordinate) {
		initialize(coordinate);
	}

	@Override
	public String toString() {
		return String.format("%s:%s:%s", toString(getKernel()),
				toString(getMeasurer()), toString(getScheme()));
	}

	public void initialize(Coordinate coordinate) {
		if (coordinate.contains(measurerAxis)) {
			setMeasurer(coordinate.get(measurerAxis));
		}
		if (getMeasurer() != null) {
			getMeasurer().initialize(coordinate);
		}

		if (coordinate.contains(measurementSchemeAxis)) {
			setScheme(coordinate.get(measurementSchemeAxis));
		}

		if (getScheme() != null) {
			getScheme().initialize(coordinate);
		}

		if (coordinate.contains(kernelAxis)) {
			setKernel(coordinate.get(kernelAxis));
		}
		if (getKernel() != null) {
			getKernel().initialize(coordinate);
		}

	}

	private String toString(Object o) {
		if (o == null) {
			return "null";
		}

		String result = o.getClass().getSimpleName();
		if (result.endsWith("Description")) {
			result = result.substring(0,
					result.length() - "Description".length());
		}
		return result;
	}

	/**
	 * Return the definition of the macro identified by key. If the macro was
	 * not defined in this measurement description, return the default
	 * definition. If contradicting definitions are found, raise an error
	 */
	public String getMacroDefinition(MacroKey key) {
		List<String> availableDefinitions = new ArrayList<String>();

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

	public void addAdditionalMeasurer(
			MeasurerDescriptionBase measurer) {
		getAdditionalMeasurers().add(0, measurer);
	}

	public void addValidationMeasurer(
			MeasurerDescriptionBase measurer) {
		getValidationMeasurers().add(0, measurer);
	}

	public ValidationData getValidationData() {
		return validationData;
	}

	public void setValidationData(ValidationData validationData) {
		this.validationData = validationData;
	}
}
