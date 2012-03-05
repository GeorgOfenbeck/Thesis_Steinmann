package ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a coordinate in a ParameterSpace. For each axis present in the
 * coordinate, a value is provided
 * 
 */
public class Coordinate {
	private final Map<Axis<?>, Object> coordinates;

	/**
	 * create a new coordinate based on the values provided
	 */
	public Coordinate(Map<Axis<?>, Object> values) {
		// copy the provided values
		coordinates = new TreeMap<Axis<?>, Object>(values);
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
		if (contains(axis)) {
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
					formattedValue(entry.getKey())));
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

	public <T> boolean contains(Axis<T> axis) {
		return coordinates.containsKey(axis);
	}
}