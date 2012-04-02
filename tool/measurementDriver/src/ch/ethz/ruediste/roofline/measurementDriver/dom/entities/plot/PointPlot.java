package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import java.util.*;

public class PointPlot extends Plot2D<PointPlot> {
	public static class Point {
		public double x, y;

		Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	public LinkedHashMap<String, LinkedList<Point>> series = new LinkedHashMap<String, LinkedList<Point>>();

	public void addValue(String seriesName, double x, double y) {
		if (!series.containsKey(seriesName))
			series.put(seriesName, new LinkedList<PointPlot.Point>());
		series.get(seriesName).add(new Point(x, y));
	}
}
