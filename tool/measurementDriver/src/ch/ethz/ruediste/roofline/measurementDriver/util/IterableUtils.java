package ch.ethz.ruediste.roofline.measurementDriver.util;

import java.util.*;

public class IterableUtils {

	public static <T> T single(Iterable<T> iterable) {
		return single(iterable, UnaryPredicates.<T> True());
	}

	public static <T> T foldr(Iterable<T> iterable, T start,
			final IBinaryFunction<T, T, T> func) {
		return foldl(reverse(iterable), start, new IBinaryFunction<T, T, T>() {
			public T apply(T arg1, T arg2) {
				return func.apply(arg2, arg1);
			}
		});
	}

	public static <T> T foldl(Iterable<T> iterable, T start,
			IBinaryFunction<T, T, T> func) {
		T result = start;
		for (T item : iterable) {
			result = func.apply(result, item);
		}
		return result;
	}

	public static <T> Iterable<T> reverse(Iterable<T> iterable) {
		LinkedList<T> result = new LinkedList<T>();
		for (T item : iterable) {
			result.addFirst(item);
		}
		return result;
	}

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

	public static <T> T singleOrDefault(Iterable<T> iterable) {
		return singleOrDefault(iterable, UnaryPredicates.<T> True());
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

	public static <T> Iterable<T> where(Iterable<T> iterable,
			IUnaryPredicate<T> predicate) {
		ArrayList<T> result = new ArrayList<T>();
		for (T item : iterable) {
			if (predicate.apply(item)) {
				result.add(item);
			}
		}
		return result;
	}

	public static <T> boolean any(Iterable<T> iterable,
			IUnaryPredicate<T> predicate) {
		return !isEmpty(where(iterable, predicate));
	}

	public static boolean any(Iterable<Boolean> iterable) {
		return !isEmpty(where(iterable, UnaryPredicates.identity()));
	}

	public static <T> boolean isEmpty(Iterable<T> iterable) {
		boolean hasNext = iterable.iterator().hasNext();
		return !hasNext;
	}

	public static <T> int indexOfSingle(Iterable<T> iterable,
			IUnaryPredicate<T> predicate) {
		int index = indexOfSingleOrDefault(iterable, predicate);

		if (index == -1) {
			throw new Error("No Match found");
		}
		return index;
	}

	public static <T> int indexOfSingleOrDefault(Iterable<T> iterable,
			IUnaryPredicate<T> predicate) throws Error {
		int index = -1;
		int i = 0;
		for (T item : iterable) {
			if (predicate.apply(item)) {
				if (index == -1) {
					index = i;
				}
				else {
					throw new Error("Multiple matches found");
				}
			}
			i++;
		}
		return index;
	}

	public static <T> T first(
			Iterable<T> iterable) {
		return first(iterable, UnaryPredicates.<T> True());
	}

	public static <T> T first(
			Iterable<T> iterable, IUnaryPredicate<T> predicate) {

		Iterator<T> it = iterable.iterator();
		while (it.hasNext()) {
			T item = it.next();
			if (predicate.apply(item)) {
				return item;
			}
		}
		throw new Error("no matching element found");
	}

	public static <T> T firstOrDefault(
			Iterable<T> iterable, IUnaryPredicate<T> predicate) {

		Iterator<T> it = iterable.iterator();
		while (it.hasNext()) {
			T item = it.next();
			if (predicate.apply(item)) {
				return item;
			}
		}

		return null;
	}

}
