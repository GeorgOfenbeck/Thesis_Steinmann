package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.toList;

import java.util.*;

public class RooflineSeries {
	public RooflineSeries(String name) {
		super();
		this.name = name;
	}

	private final String name;
	private final Map<Long, RooflinePoint> points = new HashMap<Long, RooflinePoint>();

	public List<RooflinePoint> getPoints() {

		List<RooflinePoint> list = toList(points.values());
		Collections.sort(list, new Comparator<RooflinePoint>() {

			public int compare(RooflinePoint o1, RooflinePoint o2) {
				return ((Long) o1.getProblemSize()).compareTo(o2
						.getProblemSize());
			}
		});
		return list;
	}

	public String getName() {
		return name;
	}

	public RooflinePoint addPoint(RooflinePoint point) {
		// get the problem size of the point
		long problemSize = point.getProblemSize();

		// check if a point for the problem size is already present
		if (points.containsKey(problemSize)) {
			// get the existing point 
			RooflinePoint existingPoint = points.get(problemSize);

			// merge the existing point with the new point
			existingPoint.merge(point);

			return existingPoint;
		}
		else {
			// add a new point
			points.put(problemSize, point);

			return point;
		}
	}

	public List<Long> getProblemSizes() {
		// create a sorted list from the problem sizes
		ArrayList<Long> sizeList = new ArrayList<Long>();
		sizeList.addAll(points.keySet());
		Collections.sort(sizeList);

		return sizeList;
	}

	public RooflinePoint getPoint(long problemSize) {
		return points.get(problemSize);
	}

	public boolean anyPointWithMultipleValues() {
		boolean result = false;
		for (RooflinePoint point : points.values())
			result = result || point.getN() > 1;
		return result;
	}

}
