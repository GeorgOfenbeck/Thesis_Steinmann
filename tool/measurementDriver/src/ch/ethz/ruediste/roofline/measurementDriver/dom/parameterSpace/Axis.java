package ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace;

import java.util.UUID;

import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryFunction;

public class Axis<T> implements Comparable<Axis<?>> {

	public static <T> IUnaryFunction<T, String> classNameFormatter() {
		return new IUnaryFunction<T, String>() {
			public String apply(T arg) {
				return arg.getClass().getSimpleName();
			}
		};
	}

	public static <T> IUnaryFunction<Class<? extends T>, String> clazzNameFormatter() {
		return new IUnaryFunction<Class<? extends T>, String>() {
			public String apply(Class<? extends T> arg) {
				return arg.getSimpleName();
			}
		};
	}

	public static final IUnaryFunction<Class<?>, String> clazzNameFormatter = new IUnaryFunction<Class<?>, String>() {

		public String apply(Class<?> arg) {
			return arg.getSimpleName();
		}
	};

	private final String name;
	private final IUnaryFunction<T, String> formatter;
	private final UUID uid;

	public Axis(String uid, String name) {
		this(uid, name, null);
	};

	public Axis(String uid, String name,
			IUnaryFunction<T, String> formatter) {
		super();
		this.name = name;
		this.formatter = formatter;
		this.uid = UUID.fromString(uid);
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
