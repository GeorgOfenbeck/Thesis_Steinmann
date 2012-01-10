package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RooflinePlot extends Plot2D {

	private final ArrayList<Bandwidth> peakBandwidths = new ArrayList<Bandwidth>();
	private final ArrayList<Performance> peakPerformances = new ArrayList<Performance>();
	private final ArrayList<RooflinePoint> points = new ArrayList<RooflinePoint>();

	public void addPeakBandwidth(Bandwidth peak) {
		peakBandwidths.add(peak);
	}

	public void addPeakPerformance(Performance peak) {
		peakPerformances.add(peak);
	}

	public void addPoint(RooflinePoint point) {
		points.add(point);
	}

	public List<Bandwidth> getPeakBandwiths() {
		return Collections.unmodifiableList(peakBandwidths);
	}

	public List<Performance> getPeakPerformances() {
		return Collections.unmodifiableList(peakPerformances);
	}

	public List<RooflinePoint> getPoints() {
		return Collections.unmodifiableList(points);
	}
}
