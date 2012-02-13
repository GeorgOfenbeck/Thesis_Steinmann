package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;

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

	public Iterable<OperationalIntensity> getOperationalIntensities() {
		return IterableUtils.select(getPoints(),
				new IUnaryFunction<RooflinePoint, OperationalIntensity>() {

					public OperationalIntensity apply(RooflinePoint arg) {
						return arg.getOperationalIntensity();
					}
				});
	}

	public Iterable<Performance> getPerformances() {
		ArrayList<Performance> result = new ArrayList<Performance>();
		for (RooflinePoint point : points) {
			result.add(point.getPerformance());
		}
		for (Pair<String, Performance> perf : peakPerformances) {
			result.add(perf.getRight());
		}
		return result;
	}

}
