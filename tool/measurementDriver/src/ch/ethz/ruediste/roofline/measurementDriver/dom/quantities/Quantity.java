package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

import java.util.Collections;

import ch.ethz.ruediste.roofline.measurementDriver.dom.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;
import ch.ethz.ruediste.roofline.sharedEntities.MeasurerOutputBase;

public abstract class Quantity<TDerived extends Quantity<TDerived>> implements
		Comparable<Quantity<TDerived>> {

	private double value;

	protected Quantity(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return Double.toString(getValue());
	}

	public static IBinaryPredicate<Quantity<?>, Quantity<?>> lessThanUntyped() {
		return new IBinaryPredicate<Quantity<?>, Quantity<?>>() {

			public Boolean apply(Quantity<?> arg1, Quantity<?> arg2) {
				return arg1.getValue() < arg2.getValue();
			}
		};
	}

	public static IBinaryPredicate<Quantity<?>, Quantity<?>> moreThanUntyped() {
		return new IBinaryPredicate<Quantity<?>, Quantity<?>>() {

			public Boolean apply(Quantity<?> arg1, Quantity<?> arg2) {
				return arg1.getValue() > arg2.getValue();
			}
		};
	}

	public static <T extends Quantity<T>> IBinaryPredicate<T, T> lessThan() {
		return new IBinaryPredicate<T, T>() {

			public Boolean apply(T arg1, T arg2) {
				return arg1.getValue() < arg2.getValue();
			}
		};
	}

	public static <T extends Quantity<T>> IBinaryPredicate<T, T> moreThan() {
		return new IBinaryPredicate<T, T>() {

			public Boolean apply(T arg1, T arg2) {
				return arg1.getValue() > arg2.getValue();
			}
		};
	}

	public int compareTo(Quantity<TDerived> o) {
		if (o == null)
			throw new Error("Cannot compare to null");
		if (o.getClass() != this.getClass())
			throw new Error(String.format("Cannot compare %s to %s", getClass()
					.getSimpleName(), o.getClass().getSimpleName()));
		return Double.compare(getValue(), o.getValue());
	}

	/* (non-Javadoc)
	 * @see ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.IQuantity#getValue()
	 */
	public double getValue() {
		return value;
	};

	protected abstract TDerived construct(double value);

	public static <T> T construct(Class<T> clazz, double value) {
		try {
			return clazz.getConstructor(double.class).newInstance(value);
		}
		catch (Exception e) {
			throw new Error(e);
		}
	}

	public TDerived multiplied(double factor) {
		return construct(getValue() * factor);
	}

	public TDerived added(TDerived other) {
		return construct(getValue() + other.getValue());
	}

	public TDerived subtracted(TDerived other) {
		return construct(getValue() - other.getValue());
	}

	public static <T extends Quantity<T>> IUnaryFunction<MeasurerOutputBase, T> resultToQuantity(
			final QuantityCalculator<T> calc) {
		return new IUnaryFunction<MeasurerOutputBase, T>() {

			public T apply(MeasurerOutputBase output) {
				return calc.getResult(Collections.singleton(output));
			}

		};
	}

}
