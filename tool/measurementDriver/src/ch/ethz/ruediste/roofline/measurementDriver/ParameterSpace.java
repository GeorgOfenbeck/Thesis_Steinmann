package ch.ethz.ruediste.roofline.measurementDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ParameterSpace implements
		Iterable<ParameterSpace.Coordinate> {

	public static class Coordinate {
		public Coordinate(Map<Class<? extends IAxisBase>, Object> values) {
			// copy the provided values
			coordinates = new HashMap<Class<? extends IAxisBase>, Object>(
					values);
		}

		private Map<Class<? extends IAxisBase>, Object> coordinates;

		@SuppressWarnings("unchecked")
		public <T> T get(Class<? extends IAxis<T>> axis) {
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

		@SuppressWarnings("rawtypes")
		public Coordinate project(Class... axes) {
			HashSet<Class> selectedAxes = new HashSet<Class>(
					Arrays.asList(axes));

			Map<Class<? extends IAxisBase>, Object> newCoordinates = new HashMap<Class<? extends IAxisBase>, Object>();

			for (Entry<Class<? extends IAxisBase>, Object> entry : coordinates
					.entrySet()) {
				if (selectedAxes.contains(entry.getKey())) {
					newCoordinates.put(entry.getKey(), entry.getValue());
				}
			}

			return new Coordinate(newCoordinates);
		}
	}

	private Map<Class<? extends IAxisBase>, List<Object>> axisValueSets = new HashMap<Class<? extends IAxisBase>, List<Object>>();

	public <T> T get(Class<IAxis<T>> axis) {

		return null;

	}

	public <T> void add(Class<? extends IAxis<T>> axis, T point) {
		// get or create the entry in the points map
		List<Object> list;
		if (axisValueSets.containsKey(axis)) {
			list = axisValueSets.get(axis);
		}
		else
		{
			list = new ArrayList<Object>();
			axisValueSets.put(axis, list);
		}

		list.add(point);
	}

	public Iterator<Coordinate> iterator() {
		List<Entry<Class<? extends IAxisBase>, List<Object>>> axisValueSetsList = new ArrayList<Map.Entry<Class<? extends IAxisBase>, List<Object>>>(
				axisValueSets.entrySet());

		List<Coordinate> coordinates = new ArrayList<ParameterSpace.Coordinate>();

		fillCoordinateList(coordinates, axisValueSetsList, 0,
				new HashMap<Class<? extends IAxisBase>, Object>());

		return coordinates.iterator();
	}

	private void fillCoordinateList(
			List<Coordinate> coordinates,
			List<Entry<Class<? extends IAxisBase>, List<Object>>> axisValueSetsList,
			int axisValueSetIndex,
			Map<Class<? extends IAxisBase>, Object> values) {

		if (axisValueSetIndex >= axisValueSetsList.size()) {
			coordinates.add(new Coordinate(values));
			return;
		}

		Entry<Class<? extends IAxisBase>, List<Object>> axisValues = axisValueSetsList
				.get(axisValueSetIndex);

		for (Object value : axisValues.getValue()) {
			values.put(axisValues.getKey(), value);

			fillCoordinateList(coordinates, axisValueSetsList,
					axisValueSetIndex + 1, values);
		}
		values.remove(axisValues.getKey());
	}

	@SuppressWarnings("rawtypes")
	public List<Coordinate> getCoordinates(Class... axes) {
		HashSet<Class> selectedAxes = new HashSet<Class>(Arrays.asList(axes));

		List<Entry<Class<? extends IAxisBase>, List<Object>>> axisValueSetsList = new ArrayList<Map.Entry<Class<? extends IAxisBase>, List<Object>>>();
		for (Entry<Class<? extends IAxisBase>, List<Object>> entry : axisValueSets
				.entrySet()) {
			if (selectedAxes.contains(entry.getKey())) {
				axisValueSetsList.add(entry);
			}
		}

		List<Coordinate> coordinates = new ArrayList<ParameterSpace.Coordinate>();

		fillCoordinateList(coordinates, axisValueSetsList, 0,
				new HashMap<Class<? extends IAxisBase>, Object>());

		return coordinates;

	}
}
