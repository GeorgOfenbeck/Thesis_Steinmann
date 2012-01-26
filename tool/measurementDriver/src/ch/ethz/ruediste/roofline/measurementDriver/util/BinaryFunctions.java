package ch.ethz.ruediste.roofline.measurementDriver.util;

public class BinaryFunctions {
	public static <T extends Comparable<T>> IBinaryFunction<T, T, T> min() {
		return new IBinaryFunction<T, T, T>() {

			public T apply(T arg1, T arg2) {
				if (arg1.compareTo(arg2) < 0) {
					return arg1;
				}
				return arg2;
			}
		};
	}
}
