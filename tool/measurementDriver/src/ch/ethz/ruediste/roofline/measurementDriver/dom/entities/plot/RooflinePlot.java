package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.where;

import java.util.*;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;
import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

public class RooflinePlot extends Plot2D<RooflinePlot> {

	public enum SameSizeConnection {
		None, ByPerformance, ByOperationalIntensity
	}

	private final ArrayList<Pair<String, Throughput>> peakBandwidths = new ArrayList<Pair<String, Throughput>>();
	private final ArrayList<Pair<String, Performance>> peakPerformances = new ArrayList<Pair<String, Performance>>();
	private final LinkedHashMap<String, RooflineSeries> allSeries = new LinkedHashMap<String, RooflineSeries>();
	private RooflineSeries currentSeries;

	private boolean autoscaleX;
	private boolean autoscaleY;

	private SameSizeConnection sameSizeConnection = SameSizeConnection.None;

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
						return arg.getMedianOperationalIntensity();
					}
				});
	}

	public Iterable<Performance> getPerformances() {
		ArrayList<Performance> result = new ArrayList<Performance>();
		for (RooflinePoint point : getAllPoints()) {
			result.add(point.getMedianPerformance());
		}
		for (Pair<String, Performance> perf : peakPerformances) {
			result.add(perf.getRight());
		}
		return result;
	}

	@Override
	public Range<Double> getXRange(SystemInformation systemInformation) {
		Range<OperationalIntensity> operationalIntensityRange = IterableUtils
				.getRange(getOperationalIntensities());

		// get the default range
		Range<Double> defaultRange;

		if (isAutoscaleX()) {
			// if in autoscale mode, the default range is determined by autoscaling
			defaultRange = Range.between(operationalIntensityRange.getMinimum()
					.getValue() / 2, operationalIntensityRange.getMaximum()
					.getValue() * 2);
		}
		else {
			// otherwise there are constants
			switch (systemInformation.CpuType) {
			case Core:
				defaultRange = Range.between(0.03, 100.);
			break;
			case Yonah:
				defaultRange = Range.between(0.03, 50.);
			break;
			default:
				throw new Error("Cpu Type not supported");
			}
		}

		// combine default range with range set on the plot
		return combineRanges(super.getXRange(systemInformation), defaultRange);
	}

	/**
	 * Combine two ranges
	 * 
	 * @param definedRange
	 *            if min or max is set to NaN, use default range instead
	 * @param defaultRange
	 *            range to use if min or max of defined range are NaN
	 * @return
	 */
	private Range<Double> combineRanges(Range<Double> definedRange,
			Range<Double> defaultRange) {
		double min = definedRange.getMinimum();
		if (Double.isNaN(min))
			min = defaultRange.getMinimum();

		double max = definedRange.getMaximum();
		if (Double.isNaN(max))
			max = defaultRange.getMaximum();

		return Range.between(min, max);
	}

	@Override
	public Range<Double> getYRange(SystemInformation systemInformation) {
		Range<Performance> performanceRange = IterableUtils
				.getRange(getPerformances());
		// get the default range
		Range<Double> defaultRange;

		if (isAutoscaleY()) {
			// if in autoscale mode, the default range is determined by autoscaling
			defaultRange = Range.between(performanceRange.getMinimum()
					.getValue() / 2,
					performanceRange.getMaximum().getValue() * 2);
		}
		else {
			// otherwise there are constants
			switch (systemInformation.CpuType) {
			case Core:
				defaultRange = Range.between(0.03, 20.);
			break;
			case Yonah:
				defaultRange = Range.between(0.1, 4.5);
			break;
			default:
				throw new Error("Cpu Type not supported");
			}
		}

		// combine default range with range set on the plot
		return combineRanges(super.getYRange(systemInformation), defaultRange);
	}

	public Collection<RooflineSeries> getAllSeries() {
		return allSeries.values();
	}

	public boolean isAutoscaleX() {
		return autoscaleX;
	}

	public RooflinePlot setAutoscaleX(boolean autoscaleX) {
		this.autoscaleX = autoscaleX;
		return This();
	}

	public boolean isAutoscaleY() {
		return autoscaleY;
	}

	public RooflinePlot setAutoscaleY(boolean autoscaleY) {
		this.autoscaleY = autoscaleY;
		return This();
	}

	public SameSizeConnection getSameSizeConnection() {
		return sameSizeConnection;
	}

	public RooflinePlot setSameSizeConnection(
			SameSizeConnection sameSizeConnection) {
		this.sameSizeConnection = sameSizeConnection;
		return This();
	}

	public Set<Long> getProblemSizes() {
		HashSet<Long> result = new HashSet<Long>();
		for (RooflinePoint point : getAllPoints()) {
			result.add(point.getProblemSize());
		}
		return result;
	}

	public Iterable<RooflinePoint> getPointsForProblemSize(final long size) {
		return where(getAllPoints(), new IUnaryPredicate<RooflinePoint>() {
			public Boolean apply(RooflinePoint arg) {
				return arg.getProblemSize() == size;
			}
		});
	}
}
