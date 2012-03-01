package ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.util.ClassFinder;

public class AxisReflectionHelper {
	private static HashMap<UUID, Axis<?>> axes;

	@SuppressWarnings("rawtypes")
	public static HashMap<UUID, Axis<?>> getAxes() {
		if (axes == null) {
			axes = new HashMap<UUID, Axis<?>>();
			List<Pair<Class<?>, Axis>> all = ClassFinder.getStaticFieldValues(
					Axis.class, "ch.ethz.ruediste.roofline");
			for (Pair<Class<?>, Axis> pair : all) {
				axes.put(pair.getRight().getUid(), pair.getRight());
			}
		}
		return axes;
	}

	public static Axis<?> getAxis(String uid) {
		return getAxes().get(UUID.fromString(uid));
	}
}
