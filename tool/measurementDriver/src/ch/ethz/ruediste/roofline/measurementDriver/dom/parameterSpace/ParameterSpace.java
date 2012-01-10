package ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents the a parameter space. The Space has one or more axes. Every axis
 * can take multiple values. An axis is represented by a class derving from
 * IAxis&lt;T>. T is the type of the values of the axis
 * 
 */
public class ParameterSpace implements
		Iterable<ParameterSpace.Coordinate> {

	/**
	 * Represents a coordinate in a ParameterSpace. For each axis present in the
	 * coordinate, a value is provided
	 * 
	 */
	public static class Coordinate {
		private Map<Axis<?>, Object> coordinates;

		/**
		 * create a new coordinate based on the values provided
		 */
		public Coordinate(Map<Axis<?>, Object> values) {
			// copy the provided values
			coordinates = new HashMap<Axis<?>, Object>(
					values);
		}

		/**
		 * get the value of the specified axis
		 */
		@SuppressWarnings("unchecked")
		public <T> T get(Axis<T> axis) {
			return (T) coordinates.get(axis);
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
		public Coordinate project(Axis<?>... axes) {
			HashSet<Axis<?>> selectedAxes = new HashSet<Axis<?>>(
					Arrays.asList(axes));

			Map<Axis<?>, Object> newCoordinates = new HashMap<Axis<?>, Object>();

			for (Entry<Axis<?>, Object> entry : coordinates
					.entrySet()) {
				if (selectedAxes.contains(entry.getKey())) {
					newCoordinates.put(entry.getKey(), entry.getValue());
				}
			}

			return new Coordinate(newCoordinates);
		}
	}

	private Map<Axis<?>, List<Object>> axisValueSets = new HashMap<Axis<?>, List<Object>>();

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
	public void add(Axis<?> axis,
			Collection<? extends Object> value) {
		List<Object> list = getValueListOfAxis(axis);

		list.addAll(value);
	}

	/**
	 * Returns an iterator over all coordinates in the space
	 */
	public Iterator<Coordinate> iterator() {
		List<Entry<Axis<?>, List<Object>>> axisValueSetsList = new ArrayList<Map.Entry<Axis<?>, List<Object>>>(
				axisValueSets.entrySet());

		List<Coordinate> coordinates = new ArrayList<ParameterSpace.Coordinate>();

		fillCoordinateList(coordinates, axisValueSetsList, 0,
				new HashMap<Axis<?>, Object>());

		return coordinates.iterator();
	}

	public ParameterSpace project(Axis<?>... axes) {
		ParameterSpace result = new ParameterSpace();

		// build a set from the provided axes, for fast access
		HashSet<Axis<?>> selectedAxes = new HashSet<Axis<?>>(
				Arrays.asList(axes));

		// iterate over all axes of the parameter space
		for (Entry<Axis<?>, List<Object>> entry : axisValueSets
				.entrySet()) {
			// if the axis is selected, the axis and all its values to the
			// result
			if (selectedAxes.contains(entry.getKey())) {
				result.add(entry.getKey(), entry.getValue());
			}
		}

		return result;

	}

	/**
	 * recursive helper function to iterate over all coordinates in the space
	 * 
	 * @param coordinates
	 *            list to add the coordinates to
	 * @param axisValueSetsList
	 *            list of all axes and their values
	 * @param axisValueSetIndex
	 *            axis index to process
	 * @param values
	 *            values of the axes already processed
	 */
	private void fillCoordinateList(
			List<Coordinate> coordinates,
			List<Entry<Axis<?>, List<Object>>> axisValueSetsList,
			int axisValueSetIndex,
			Map<Axis<?>, Object> values) {

		// if the last axis has been processed already (and a value was added
		// for it to values),
		// build a coordinate from the gathered values
		if (axisValueSetIndex >= axisValueSetsList.size()) {
			coordinates.add(new Coordinate(values));
			return;
		}

		// get the axis to be processed, along with it's values
		Entry<Axis<?>, List<Object>> axisValuesPair = axisValueSetsList
				.get(axisValueSetIndex);

		// iterate over all values for the axis
		for (Object value : axisValuesPair.getValue()) {
			// put the value and the axis to the values map
			values.put(axisValuesPair.getKey(), value);

			// do the recursion
			fillCoordinateList(coordinates, axisValueSetsList,
					axisValueSetIndex + 1, values);
		}

		// remove the entry for the processed axis from the value map
		values.remove(axisValuesPair.getKey());
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
		}
		else
		{
			// otherwise create a new list and store it in the map
			list = new ArrayList<Object>();
			axisValueSets.put(axis, list);
		}
		return list;
	}
}
