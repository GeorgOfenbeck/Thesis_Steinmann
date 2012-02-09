package ch.ethz.ruediste.roofline.dom;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class Measurement extends MeasurementData {

	@XStreamOmitField
	private ValidationData validationData;

	public Measurement() {
	}

	public Measurement(Coordinate coordinate) {
		initialize(coordinate);
	}

	@Override
	public String toString() {

		return StringUtils.join(getWorkloads(), ":");
	}

	public void initialize(Coordinate coordinate) {
		for (Workload workload : getWorkloads()) {
			workload.initialize(coordinate);
		}

	}

	/**
	 * Return the definition of the macro identified by key. If the macro was
	 * not defined in this measurement description, return the default
	 * definition. If contradicting definitions are found, raise an error
	 */
	public String getMacroDefinition(MacroKey key) {
		List<String> availableDefinitions = new ArrayList<String>();

		// add eventual definition of this
		if (super.isMacroDefined(key)) {
			availableDefinitions.add(super.getMacroDefinition(key));
		}

		// get the macro definitions of all workloads
		for (Workload workload : getWorkloads()) {
			availableDefinitions.addAll(workload.getMacroDefinitions(key));
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

	public ValidationData getValidationData() {
		return validationData;
	}

	public void setValidationData(ValidationData validationData) {
		this.validationData = validationData;
	}

	public void addWorkload(Workload workload) {
		getWorkloads().add(workload);
	}
}
