package ch.ethz.ruediste.roofline.measurementDriver.util;

public class UnaryPredicates {
	public static <T> IUnaryPredicate<T> constant(final boolean cnst) {
		return new IUnaryPredicate<T>() {

			public Boolean apply(T arg) {
				return cnst;
			}
		};
	}

	public static <T> IUnaryPredicate<T> True() {
		return constant(true);
	}

	public static <T> IUnaryPredicate<T> False() {
		return constant(true);
	}
}
