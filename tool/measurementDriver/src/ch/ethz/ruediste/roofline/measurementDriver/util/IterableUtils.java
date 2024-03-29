package ch.ethz.ruediste.roofline.measurementDriver.util;

import java.util.*;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;

public class IterableUtils {

	public static <T> T single(Iterable<T> iterable) {
		return single(iterable, UnaryPredicates.<T> True());
	}

	public static <T, R> R foldr(Iterable<T> iterable, R start,
			final IBinaryFunction<T, R, R> func) {
		return foldl(reverse(iterable), start, new IBinaryFunction<R, T, R>() {
			public R apply(R arg1, T arg2) {
				return func.apply(arg2, arg1);
			}
		});
	}

	public static <T, R> R foldl(Iterable<T> iterable, R start,
			IBinaryFunction<R, T, R> func) {
		R result = start;
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

	public static <T> Iterable<T> order(Iterable<T> iterable,
			IBinaryPredicate<T, T> before) {
		List<T> result = toList(iterable);
		Collections.sort(result, BinaryPredicates.getComparator(before));
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

	public static <T> T single(T[] items, IUnaryPredicate<T> predicate) {
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

	public static <T> Iterable<T> tail(Iterable<T> iterable) {
		if (isEmpty(iterable)) {
			throw new Error("cannot get the tail of an empty list");
		}

		ArrayList<T> result = new ArrayList<T>();
		boolean first = true;
		Iterator<T> it = iterable.iterator();
		while (it.hasNext()) {
			T item = it.next();
			if (first) {
				first = false;
			}
			else {
				result.add(item);
			}
		}
		return result;
	}

	public static <T> T head(Iterable<T> iterable) {
		return first(iterable);
	}

	public static <T> T first(Iterable<T> iterable) {
		return first(iterable, UnaryPredicates.<T> True());
	}

	public static <T> T last(Iterable<T> iterable) {
		boolean found = false;
		T result = null;
		Iterator<T> it = iterable.iterator();
		while (it.hasNext()) {
			result = it.next();
			found = true;
		}
		if (found) {
			return result;
		}
		throw new Error("iterable was empty");
	}

	public static <T> T first(Iterable<T> iterable, IUnaryPredicate<T> predicate) {

		Iterator<T> it = iterable.iterator();
		while (it.hasNext()) {
			T item = it.next();
			if (predicate.apply(item)) {
				return item;
			}
		}
		throw new Error("no matching element found");
	}

	public static <T> T firstOrDefault(Iterable<T> iterable,
			IUnaryPredicate<T> predicate) {

		Iterator<T> it = iterable.iterator();
		while (it.hasNext()) {
			T item = it.next();
			if (predicate.apply(item)) {
				return item;
			}
		}

		return null;
	}

	public static <T, R> Iterable<R> select(Iterable<T> iterable,
			IUnaryFunction<T, R> func) {
		ArrayList<R> result = new ArrayList<R>();
		for (T item : iterable) {
			result.add(func.apply(item));
		}
		return result;
	}

	public static <T, R> Iterable<R> selectAll(Iterable<T> iterable,
			IUnaryFunction<T, Iterable<R>> func) {
		ArrayList<R> result = new ArrayList<R>();
		for (T item : iterable) {
			addAll(result, func.apply(item));
		}
		return result;
	}

	public static <T> T min(Iterable<T> iterable,
			IBinaryPredicate<T, T> comparator) {
		return getRange(iterable, comparator).getMinimum();
	}

	public static <T extends Comparable<T>> T min(Iterable<T> iterable) {
		return getRange(iterable).getMinimum();
	}

	public static <T extends Comparable<? super T>> Range<T> getRange(
			Iterable<T> iterable) {
		return getRange(iterable, BinaryPredicates.<T> getComparator());
	}

	public static <T> Range<T> getRange(Iterable<T> iterable,
			IBinaryPredicate<T, T> comparator) {
		T min = null;
		T max = null;
		boolean found = false;

		for (T item : iterable) {
			if (!found) {
				min = item;
				max = item;
				found = true;
			}
			else {
				if (comparator.apply(item, min)) {
					min = item;
				}
				if (comparator.apply(max, item)) {
					max = item;
				}
			}
		}

		if (!found)
			throw new Error("iterable was empty");

		return Range.between(min, max,
				BinaryPredicates.getComparator(comparator));
	}

	public static <T> List<T> toList(Iterable<T> iterable) {
		ArrayList<T> result = new ArrayList<T>();
		addAll(result, iterable);
		return result;
	}

	public static <T> List<T> toList(T... items) {
		ArrayList<T> result = new ArrayList<T>();
		addAll(result, items);
		return result;
	}

	public static <T> void addAll(Collection<T> coll, T... array) {
		for (T item : array) {
			coll.add(item);
		}
	}

	public static <T> void addAll(Collection<T> coll, Iterable<T> iterable) {
		for (T item : iterable) {
			coll.add(item);
		}
	}

	public static <L, R> Iterable<Pair<L, R>> zip(Iterable<L> left,
			Iterable<R> right) {
		ArrayList<Pair<L, R>> result = new ArrayList<Pair<L, R>>();
		Iterator<L> leftI = left.iterator();
		Iterator<R> rightI = right.iterator();

		while (leftI.hasNext() && rightI.hasNext()) {
			result.add(Pair.of(leftI.next(), rightI.next()));
		}

		if (leftI.hasNext() || rightI.hasNext()) {
			throw new Error("Iterables were of unequal length");
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> ofType(Class<T> clazz, Iterable<?> iterable) {
		ArrayList<T> result = new ArrayList<T>();
		for (Object obj : iterable) {
			if (obj != null && clazz.isInstance(obj)) {
				result.add((T) obj);
			}
		}
		return result;
	}
}
