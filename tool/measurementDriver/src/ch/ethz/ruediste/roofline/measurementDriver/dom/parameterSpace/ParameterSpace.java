package ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/**
 * Represents the a parameter space. The Space has one or more axes. Every axis
 * can take multiple values. An axis is represented by a class derving from
 * IAxis&lt;T>. T is the type of the values of the axis
 * 
 */
public class ParameterSpace implements Iterable<ParameterSpace.Coordinate> {

	/**
	 * Represents a coordinate in a ParameterSpace. For each axis present in the
	 * coordinate, a value is provided
	 * 
	 */
	public static class Coordinate {
		private final Map<Axis<?>, Object> coordinates;

		/**
		 * create a new coordinate based on the values provided
		 */
		public Coordinate(Map<Axis<?>, Object> values) {
			// copy the provided values
			coordinates = new HashMap<Axis<?>, Object>(values);
		}

		public final static Coordinate EMPTY;

		static {
			EMPTY = new Coordinate(new HashMap<Axis<?>, Object>());
		}

		/**
		 * get the value of the specified axis
		 */
		@SuppressWarnings("unchecked")
		public <T> T get(Axis<T> axis) {
			if (coordinates.containsKey(axis)) {
				return (T) coordinates.get(axis);
			}
			return axis.getDefaultValue();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Coordinate)) {
				return false;
			}
			Coordinate other = (Coordinate) obj;

			return coordinates.equals(other.coordinates);
		}

		@Override
		public int hashCode() {
			return coordinates.hashCode();
		}

		/**
		 * return the projection of this coordinate to the specified axes. The
		 * axes not specified are discarded.
		 */
		public Coordinate getProjection(Axis<?>... axes) {
			HashSet<Axis<?>> selectedAxes = new HashSet<Axis<?>>(
					Arrays.asList(axes));

			Map<Axis<?>, Object> newCoordinates = new HashMap<Axis<?>, Object>();

			for (Entry<Axis<?>, Object> entry : coordinates.entrySet()) {
				if (selectedAxes.contains(entry.getKey())) {
					newCoordinates.put(entry.getKey(), entry.getValue());
				}
			}

			return new Coordinate(newCoordinates);
		}

		public <T> Coordinate getExtendedPoint(Axis<T> axis, T value) {
			if (coordinates.containsKey(axis)) {
				throw new Error("Axis already part of the coordinate");
			}

			HashMap<Axis<?>, Object> newMap = new HashMap<Axis<?>, Object>(
					coordinates);
			newMap.put(axis, value);
			return new Coordinate(newMap);
		}

		public <T> Coordinate getMovedPoint(Axis<T> axis, T value) {
			if (!coordinates.containsKey(axis)) {
				throw new Error("Axis is not part of the coordinate");
			}

			HashMap<Axis<?>, Object> newMap = new HashMap<Axis<?>, Object>(
					coordinates);
			newMap.put(axis, value);
			return new Coordinate(newMap);
		}

		@Override
		public String toString() {
			ArrayList<String> parts = new ArrayList<String>();
			for (Entry<Axis<?>, Object> entry : coordinates.entrySet()) {
				parts.add(String.format("%s=%s", entry.getKey(),
						entry.getValue()));
			}
			return StringUtils.join(parts, ", ");
		}

		public String toString(Axis<?>... axes) {
			ArrayList<String> parts = new ArrayList<String>();
			for (Axis<?> axis : axes) {
				parts.add(String.format("%s=%s", axis, formattedValue(axis)));
			}
			return StringUtils.join(parts, ", ");
		}

		public <T> String formattedValue(Axis<T> axis) {
			return axis.format(get(axis));
		}
	}

	private final Map<Axis<?>, List<Object>> axisValueSets = new HashMap<Axis<?>, List<Object>>();

	/**
	 * Add a value to an axis
	 * 
	 * @param axis
	 *            the axis to add the value to
	 * @param value
	 *            the value to add to the axis
	 */
	public <T> void add(Axis<T> axis, T value) {
		List<Object> list = getValueListOfAxis(axis);

		list.add(value);
	}

	/**
	 * Add a list of values to the given axis
	 * 
	 * @param axis
	 *            axis to add the values to
	 * @param value
	 *            values to add to the axis
	 */
	public void add(Axis<?> axis, Collection<? extends Object> value) {
		List<Object> list = getValueListOfAxis(axis);

		list.addAll(value);
	}

	/**
	 * Returns an iterator over all coordinates in the space
	 */
	public Iterator<Coordinate> iterator() {
		return getAllPoints().iterator();
	}

	public ParameterSpace getProjection(List<Axis<?>> axes) {
		return getProjection(axes.toArray(new Axis<?>[0]));
	}

	public ParameterSpace getProjection(Axis<?>... axes) {
		ParameterSpace result = new ParameterSpace();

		// build a set from the provided axes, for fast access
		HashSet<Axis<?>> selectedAxes = new HashSet<Axis<?>>(
				Arrays.asList(axes));

		// iterate over all axes of the parameter space
		for (Entry<Axis<?>, List<Object>> entry : axisValueSets.entrySet()) {
			// if the axis is selected, the axis and all its values to the
			// result
			if (selectedAxes.contains(entry.getKey())) {
				result.add(entry.getKey(), entry.getValue());
			}
		}

		return result;

	}

	/*
	 * return all axes of the parameter space
	 */
	public List<Axis<?>> getAllAxes() {
		return new ArrayList<Axis<?>>(axisValueSets.keySet());
	}

	public List<Axis<?>> getAllAxesWithMostSignificantAxes(Axis<?>... msAxes) {
		return getAllAxesWithMostSignificantAxes(getAllAxes(), msAxes);
	}

	public List<Axis<?>> getAllAxesWithMostSignificantAxes(
			List<Axis<?>> allAxes, Axis<?>... msAxes) {
		// build the result list with the first axes
		List<Axis<?>> result = new ArrayList<Axis<?>>();
		for (Axis<?> axis : msAxes) {
			if (!axisValueSets.containsKey(axis)) {
				throw new Error("axis not part of the parameter space");
			}
			result.add(axis);
		}

		// get all axes and remove the axes which are already named by msAxes
		List<Axis<?>> axes = new ArrayList<Axis<?>>(allAxes);
		axes.removeAll(new HashSet<Axis<?>>(result));

		// append the missing axes to the result
		for (Axis<?> axis : axes) {
			result.add(axis);
		}

		return result;
	}

	public List<Axis<?>> getAllAxesWithLeastSignificantAxes(Axis<?>... lsAxes) {
		return getAllAxesWithLeastSignificantAxes(getAllAxes(), lsAxes);
	}

	public List<Axis<?>> getAllAxesWithLeastSignificantAxes(
			List<Axis<?>> allAxes, Axis<?>... lsAxes) {
		// build the list of the least significant axes
		List<Axis<?>> lsAxesList = new ArrayList<Axis<?>>();
		for (Axis<?> axis : lsAxes) {
			if (!axisValueSets.containsKey(axis)) {
				throw new Error("axis not part of the parameter space");
			}
			lsAxesList.add(axis);
		}

		// get all axes and remove the axes which are already named by lsAxes
		List<Axis<?>> result = new ArrayList<Axis<?>>(allAxes);
		result.removeAll(new HashSet<Axis<?>>(lsAxesList));

		// append the least significant axes to the result
		for (Axis<?> axis : lsAxes) {
			result.add(axis);
		}

		return result;
	}

	public List<Coordinate> getAllPoints() {
		return getAllPoints(getAllAxes());
	}

	@SuppressWarnings("unchecked")
	public List<Coordinate> getAllPoints(List<Axis<?>> orderedAxes) {

		if (axisValueSets.isEmpty()) {
			return Collections.emptyList();
		}

		// get head and tail of the ordered Axes
		Iterator<Axis<?>> it = orderedAxes.iterator();
		Axis<?> orderedAxesHead = it.next();
		List<Axis<?>> orderedAxesTail = new ArrayList<Axis<?>>();
		while (it.hasNext()) {
			orderedAxesTail.add(it.next());
		}

		// get all points in the subspace
		List<Coordinate> subSpacePoints = getProjection(orderedAxesTail)
				.getAllPoints(orderedAxesTail);

		List<Coordinate> result = new ArrayList<ParameterSpace.Coordinate>();

		// iterate over the values of the first axis
		for (Object value : axisValueSets.get(orderedAxesHead)) {
			if (subSpacePoints.isEmpty()) {
				// add a single point for each value if the subspace is emtpy
				result.add(Coordinate.EMPTY.getExtendedPoint(
						(Axis<Object>) orderedAxesHead, value));
			} else {
				// add the value of the first axis to each point of the subspace
				for (Coordinate subSpacePoint : subSpacePoints) {
					result.add(subSpacePoint.getExtendedPoint(
							(Axis<Object>) orderedAxesHead, value));
				}
			}
		}

		return result;
	}

	/**
	 * gets or creates the list which stores the list of values for the given
	 * axis
	 * 
	 * @param axis
	 * @return
	 */
	private <T> List<Object> getValueListOfAxis(Axis<?> axis) {
		List<Object> list;
		// if the list for the axis exists already, return it
		if (axisValueSets.containsKey(axis)) {
			list = axisValueSets.get(axis);
		} else {
			// otherwise create a new list and store it in the map
			list = new ArrayList<Object>();
			axisValueSets.put(axis, list);
		}
		return list;
	}
}
