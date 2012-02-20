package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;

public class RooflineSeries {
	public RooflineSeries(String name) {
		super();
		this.name = name;
	}

	private final String name;
	private final ArrayList<RooflinePoint> points = new ArrayList<RooflinePoint>();

	public ArrayList<RooflinePoint> getPoints() {
		return points;
	}

	public String getName() {
		return name;
	}

	public void addPoint(RooflinePoint point) {
		points.add(point);
	}
}
