package ch.ethz.ruediste.roofline.dom;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class MeasurementDescription extends MeasurementDescriptionData {
	public static final Axis<MeasurementSchemeDescriptionBase> measurementSchemeAxis = new Axis<MeasurementSchemeDescriptionBase>(
			"scheme");
	public static final Axis<KernelDescriptionBase> kernelAxis = new Axis<KernelDescriptionBase>(
			"kernel");
	public static final Axis<MeasurerDescriptionBase> measurerAxis = new Axis<MeasurerDescriptionBase>(
			"measurer", null, Axis.classNameFormatter);

	public static final Axis<Long> bufferSizeAxis = new Axis<Long>(
			"bufferSize", (long) 1024 * 1024);

	public static final Axis<Long> iterationsAxis = new Axis<Long>(
			"iterations", (long) 1024 * 1024);

	public static final Axis<Integer> unrollAxis = new Axis<Integer>("unroll",
			1);
	public static final Axis<Integer> dlpAxis = new Axis<Integer>("dlp", 1);

	public static final Axis<String> operationAxis = new Axis<String>(
			"operation", "ArithmeticOperation_ADD");

	public static final Axis<String> optimizationAxis = new Axis<String>(
			"optimization", "-O3");

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
		setMeasurer(coordinate.get(measurerAxis));
		if (getMeasurer() != null) {
			getMeasurer().initialize(coordinate);
		}

		setScheme(coordinate.get(measurementSchemeAxis));
		if (getScheme() != null) {
			getScheme().initialize(coordinate);
		}
		setKernel(coordinate.get(kernelAxis));
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
}
