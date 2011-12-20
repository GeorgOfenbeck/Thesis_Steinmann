package ch.ethz.ruediste.roofline.dom;

public class MeasurementDescription extends MeasurementDescriptionData {
	@Override
	public String toString() {
		return String.format("%s:%s:%s:%s",
				toString(getKernel()),
				toString(getMeasurer()),
				toString(getScheme()),
				getOptimization());
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

	public void addMacro(String macroName, String definition) {
		PreprocessorMacro macro = new PreprocessorMacro();
		macro.setName(macroName);
		macro.setDefinition(definition);
		getMacros().add(macro);
	}
}
