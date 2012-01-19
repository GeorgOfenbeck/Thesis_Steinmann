package ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class CoordinateBuilder {
	private Map<Axis<?>, Object> map = new HashMap<Axis<?>, Object>();

	public static CoordinateBuilder createCoordinate() {
		return new CoordinateBuilder();
	}

	public <T> CoordinateBuilder set(Axis<T> axis, T value) {
		map.put(axis, value);
		return this;
	}

	public Coordinate build() {
		return new Coordinate(map);
	}
}
