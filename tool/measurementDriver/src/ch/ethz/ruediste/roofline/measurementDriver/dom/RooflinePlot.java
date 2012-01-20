package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;

public class RooflinePlot extends Plot2D {

	private final ArrayList<Pair<String, Throughput>> peakBandwidths = new ArrayList<Pair<String, Throughput>>();
	private final ArrayList<Pair<String, Performance>> peakPerformances = new ArrayList<Pair<String, Performance>>();
	private final ArrayList<RooflinePoint> points = new ArrayList<RooflinePoint>();

	public void addPeakThroughput(String name, Throughput peak) {
		peakBandwidths.add(Pair.of(name, peak));
	}

	public void addPeakPerformance(String name, Performance peak) {
		peakPerformances.add(Pair.of(name, peak));
	}

	public void addPoint(RooflinePoint point) {
		points.add(point);
	}

	public List<Pair<String, Throughput>> getPeakBandwiths() {
		return Collections.unmodifiableList(peakBandwidths);
	}

	public List<Pair<String, Performance>> getPeakPerformances() {
		return Collections.unmodifiableList(peakPerformances);
	}

	public List<RooflinePoint> getPoints() {
		return Collections.unmodifiableList(points);
	}
}
