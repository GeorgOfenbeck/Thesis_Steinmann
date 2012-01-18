package ch.ethz.ruediste.roofline.measurementDriver.util;

import java.util.*;

public class IterableUtils {
	public static <T> T single(Iterable<T> iterable, IUnaryPredicate<T> pred) {
		boolean found = false;
		T foundItem = null;

		for (T item : iterable) {
			if (pred.apply(item)) {
				if (found) {
					throw new Error("Multiple items match the predicate");
				}
				found = true;
				foundItem = item;
			}
		}
		if (!found) {
			throw new Error("No item matched the predicate");
		}
		return foundItem;
	}

	public static <T> T singleOrDefault(Iterable<T> iterable,
			IUnaryPredicate<T> pred) {
		boolean found = false;
		T foundItem = null;

		for (T item : iterable) {
			if (pred.apply(item)) {
				if (found) {
					throw new Error("Multiple items match the predicate");
				}
				found = true;
				foundItem = item;
			}
		}

		return foundItem;
	}

	public static <T> T single(T[] items,
			IUnaryPredicate<T> predicate) {
		return single(Arrays.asList(items), predicate);
	}

	public static <T> Iterable<T> where(List<T> iterable,
			IUnaryPredicate<T> predicate) {
		ArrayList<T> result = new ArrayList<T>();
		for (T item : iterable) {
			if (predicate.apply(item)) {
				result.add(item);
			}
		}
		return result;
	}
}
