package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import java.util.*;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;

public class RooflinePlot extends Plot2D<RooflinePlot> {

	private final ArrayList<Pair<String, Throughput>> peakBandwidths = new ArrayList<Pair<String, Throughput>>();
	private final ArrayList<Pair<String, Performance>> peakPerformances = new ArrayList<Pair<String, Performance>>();
	private final HashMap<String, RooflineSeries> allSeries = new LinkedHashMap<String, RooflineSeries>();
	private RooflineSeries currentSeries;

	private boolean autoscale;

	public RooflinePlot() {
		setLogX(true);
		setLogY(true);

	}

	public void addPeakThroughput(String name, Throughput peak) {
		peakBandwidths.add(Pair.of(name, peak));
	}

	public void addPeakPerformance(String name, Performance peak) {
		peakPerformances.add(Pair.of(name, peak));
	}

	private RooflineSeries getCurrentSeries() {
		return currentSeries;
	}

	public void addPoint(String seriesName, RooflinePoint point) {
		getSeries(seriesName).addPoint(point);
	}

	public void addPoint(RooflinePoint point) {
		getCurrentSeries().addPoint(point);
	}

	public void setCurrentSeries(String name) {
		currentSeries = getSeries(name);
	}

	public RooflineSeries getSeries(String name) {
		if (!allSeries.containsKey(name)) {
			allSeries.put(name, new RooflineSeries(name));
		}
		return allSeries.get(name);
	}

	public List<Pair<String, Throughput>> getPeakBandwiths() {
		return Collections.unmodifiableList(peakBandwidths);
	}

	public List<Pair<String, Performance>> getPeakPerformances() {
		return Collections.unmodifiableList(peakPerformances);
	}

	public List<RooflinePoint> getAllPoints() {
		ArrayList<RooflinePoint> result = new ArrayList<RooflinePoint>();
		for (RooflineSeries series : allSeries.values()) {
			result.addAll(series.getPoints());
		}
		return result;
	}

	public Iterable<OperationalIntensity> getOperationalIntensities() {
		return IterableUtils.select(getAllPoints(),
				new IUnaryFunction<RooflinePoint, OperationalIntensity>() {

			public OperationalIntensity apply(RooflinePoint arg) {
				return arg.getOperationalIntensity();
			}
		});
	}

	public Iterable<Performance> getPerformances() {
		ArrayList<Performance> result = new ArrayList<Performance>();
		for (RooflinePoint point : getAllPoints()) {
			result.add(point.getPerformance());
		}
		for (Pair<String, Performance> perf : peakPerformances) {
			result.add(perf.getRight());
		}
		return result;
	}

	public boolean isAutoscale() {
		return autoscale;
	}

	public RooflinePlot setAutoscale(boolean autoscale) {
		this.autoscale = autoscale;
		return this;
	}

	@Override
	public Range<Double> getXRange() {
		Range<OperationalIntensity> operationalIntensityRange = IterableUtils
				.getRange(getOperationalIntensities());

		if (isAutoscale()) {
			return Range.between(operationalIntensityRange.getMinimum()
					.getValue() / 2, operationalIntensityRange.getMaximum()
					.getValue() * 2);
		}
		else {
			return Range.between(0.03, 100.);
		}
	}

	@Override
	public Range<Double> getYRange() {
		Range<Performance> performanceRange = IterableUtils
				.getRange(getPerformances());

		// set the scaling
		if (isAutoscale()) {
			return Range.between(performanceRange.getMinimum().getValue() / 2,
					performanceRange.getMaximum().getValue() * 2);
		}
		else {
			return Range.between(0.03, 20.);

		}
	}

	public Collection<RooflineSeries> getAllSeries() {
		return allSeries.values();
	}
}
