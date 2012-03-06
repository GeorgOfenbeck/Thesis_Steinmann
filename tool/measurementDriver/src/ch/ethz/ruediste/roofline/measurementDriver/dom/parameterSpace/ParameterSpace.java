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
public class ParameterSpace implements Iterable<Coordinate> {

	private final Map<Axis<?>, List<Object>> axisValueSets = new LinkedHashMap<Axis<?>, List<Object>>();

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
	public void addAll(Axis<?> axis, Collection<? extends Object> value) {
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

	/**
	 * return a projection on the subspace identified by axes
	 * 
	 * @param axes
	 *            Axes to project on
	 * @return
	 */
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
				result.addAll(entry.getKey(), entry.getValue());
			}
		}

		return result;

	}

	/**
	 * return all axes of the parameter space
	 */
	public List<Axis<?>> getAllAxes() {
		return new ArrayList<Axis<?>>(axisValueSets.keySet());
	}

	/**
	 * return axes specified by axes, with null as the wildchar character (zero
	 * or more axes)
	 * 
	 */
	public List<Axis<?>> getAxes(Axis<?>... axes) {
		int start = 0;
		int end = axes.length;

		ArrayList<Axis<?>> result = new ArrayList<Axis<?>>();
		ArrayList<Axis<?>> endAxes = new ArrayList<Axis<?>>();

		// add all starting axes up to the star
		while (start < end && axes[start] != null) {
			result.add(axes[start]);
			start++;
		}

		// add all ending axes up to the star
		while (start < end && axes[end - 1] != null) {
			endAxes.add(axes[end - 1]);
			end--;
		}
		Collections.reverse(endAxes);

		// add the remaining axes to the result
		HashSet<Axis<?>> remainingAxes = new HashSet<Axis<?>>();
		remainingAxes.addAll(getAllAxes());
		remainingAxes.removeAll(result);
		remainingAxes.removeAll(endAxes);
		result.addAll(remainingAxes);

		// add the end axes to the result
		result.addAll(endAxes);

		return result;
	}

	/**
	 * get all points in the parameter space
	 */
	public List<Coordinate> getAllPoints() {
		return getAllPoints(getAllAxes());
	}

	/**
	 * return all points, using the specified order of the axes. null is the
	 * wildchar axis (zero or more)
	 */
	public List<Coordinate> getAllPoints(Axis<?>... axes) {
		return getAllPoints(getAxes(axes));
	}

	/**
	 * return all points, only from the axes mentioned. null is not allowed in
	 * the ordered axes
	 */
	@SuppressWarnings("unchecked")
	private List<Coordinate> getAllPoints(List<Axis<?>> orderedAxes) {

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

		List<Coordinate> result = new ArrayList<Coordinate>();

		// iterate over the values of the first axis
		for (Object value : getValues(orderedAxesHead)) {
			if (subSpacePoints.isEmpty()) {
				// add a single point for each value if the subspace is emtpy
				result.add(Coordinate.EMPTY.getExtendedPoint(
						(Axis<Object>) orderedAxesHead, value));
			}
			else {
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
	 */
	private <T> List<Object> getValueListOfAxis(Axis<?> axis) {
		List<Object> list;
		// if the list for the axis exists already, return it
		if (axisValueSets.containsKey(axis)) {
			list = axisValueSets.get(axis);
		}
		else {
			// otherwise create a new list and store it in the map
			list = new ArrayList<Object>();
			axisValueSets.put(axis, list);
		}
		return list;
	}

	@Override
	public String toString() {
		return StringUtils.join(axisValueSets.keySet(), ",");
	}

	/**
	 * return the axis which has the most associated values
	 */
	public Axis<?> getLongestAxis() {
		Axis<?> result = null;
		for (Axis<?> axis : getAllAxes()) {
			if (result == null || getAxisLength(axis) > getAxisLength(result)) {
				result = axis;
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getValues(Axis<T> axis) {
		return (List<T>) axisValueSets.get(axis);
	}

	/**
	 * get the number of values associated with an axis
	 */
	public int getAxisLength(Axis<?> axis) {
		return getValues(axis).size();
	}

	/**
	 * return all points surrounding a given coordinate
	 */
	@SuppressWarnings("unchecked")
	public List<Coordinate> getNeighbors(Coordinate middle) {
		List<Coordinate> result = new ArrayList<Coordinate>();

		for (Axis<?> axis : getAllAxes()) {
			int index = getValues(axis).indexOf(middle.get(axis));
			int prev = index - 1;
			if (prev < 0) {
				prev = getAxisLength(axis) - 1;
			}
			int next = index + 1;
			if (next >= getAxisLength(axis)) {
				next = 0;
			}
			result.add(middle.<Object> getMovedPoint((Axis<Object>) axis,
					getValues(axis).get(prev)));
			result.add(middle.<Object> getMovedPoint((Axis<Object>) axis,
					getValues(axis).get(next)));
		}
		return result;
	}
}
