package ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace;

public class Axis<T> {

	private final T defaultValue;
	private final String name;

	public Axis(String name) {
		this.name = name;
		defaultValue = null;
	};

	public Axis(String name, T defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
