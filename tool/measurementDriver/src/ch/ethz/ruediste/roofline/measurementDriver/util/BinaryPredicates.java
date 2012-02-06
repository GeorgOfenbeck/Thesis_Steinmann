package ch.ethz.ruediste.roofline.measurementDriver.util;

import java.util.Comparator;

public class BinaryPredicates {
	public static <T> Comparator<T> getComparator(
			final IBinaryPredicate<T, T> comp) {
		return new Comparator<T>() {

			public int compare(T o1, T o2) {
				if (comp.apply(o1, o2)) {
					return -1;
				}
				if (comp.apply(o2, o1)) {
					return 1;
				}
				return 0;
			}
		};
	}

	public static <T extends Comparable<? super T>> IBinaryPredicate<T, T> getComparator() {
		return new IBinaryPredicate<T, T>() {

			public Boolean apply(T arg1, T arg2) {
				return arg1.compareTo(arg2) < 0;
			}
		};
	}

	public static IBinaryPredicate<Double, Double> lessThan(Class<Double> clazz) {
		return new IBinaryPredicate<Double, Double>() {

			public Boolean apply(Double arg1, Double arg2) {
				return arg1 < arg2;
			}
		};
	}

	public static IBinaryPredicate<Double, Double> moreThan(Class<Double> clazz) {
		return new IBinaryPredicate<Double, Double>() {

			public Boolean apply(Double arg1, Double arg2) {
				return arg1 > arg2;
			}
		};
	}
}
