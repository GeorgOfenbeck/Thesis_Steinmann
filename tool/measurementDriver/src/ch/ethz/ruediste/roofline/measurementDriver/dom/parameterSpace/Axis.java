package ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace;

import ch.ethz.ruediste.roofline.dom.MeasurerDescriptionBase;

public class Axis<T> {

	public static final IUnaryFunction<MeasurerDescriptionBase, String> classNameFormatter = new IUnaryFunction<MeasurerDescriptionBase, String>() {

		public String apply(MeasurerDescriptionBase arg) {
			return arg.getClass().getSimpleName();
		}
	};
	private final T defaultValue;
	private final String name;
	private final IUnaryFunction<T, String> formatter;

	public Axis(String name) {
		this.name = name;
		defaultValue = null;
		formatter = null;
	};

	public Axis(String name, T defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
		formatter = null;
	}

	public Axis(String name, T defaultValue, IUnaryFunction<T, String> formatter) {
		super();
		this.defaultValue = defaultValue;
		this.name = name;
		this.formatter = formatter;
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

	/**
	 * formats a value to a string usable for printing
	 */
	public String format(T value) {
		if (formatter == null) {
			return String.format("%s", value);
		}

		return formatter.apply(value);
	}
}
