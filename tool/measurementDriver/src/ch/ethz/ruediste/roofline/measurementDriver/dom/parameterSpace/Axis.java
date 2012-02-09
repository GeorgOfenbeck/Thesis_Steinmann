package ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace;

import java.util.UUID;

import ch.ethz.ruediste.roofline.dom.MeasurerBase;
import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryFunction;

public class Axis<T> implements Comparable<Axis<?>> {

	public static final IUnaryFunction<MeasurerBase, String> classNameFormatter = new IUnaryFunction<MeasurerBase, String>() {

		public String apply(MeasurerBase arg) {
			return arg.getClass().getSimpleName();
		}
	};
	private final T defaultValue;
	private final String name;
	private final IUnaryFunction<T, String> formatter;
	private final UUID uid;

	public Axis(String uid, String name) {
		this(uid, name, null);
	};

	public Axis(String uid, String name, T defaultValue) {
		this(uid, name, defaultValue, null);
	}

	public Axis(String uid, String name, T defaultValue,
			IUnaryFunction<T, String> formatter) {
		super();
		this.defaultValue = defaultValue;
		this.name = name;
		this.formatter = formatter;
		this.uid = UUID.fromString(uid);
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

	/**
	 * order by name and then by instantiation number
	 */
	public int compareTo(Axis<?> o) {
		return getUid().compareTo(o.getUid());
	}

	public UUID getUid() {
		return uid;
	}
}
