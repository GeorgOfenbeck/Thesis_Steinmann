package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.addAll;

import java.util.*;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;

public class RooflinePoint {
	final private long problemSize;
	final private List<OperationalIntensity> operationalIntensities = new ArrayList<OperationalIntensity>();
	final private List<Performance> performances = new ArrayList<Performance>();
	private String label;

	public RooflinePoint(long problemSize) {
		super();
		this.problemSize = problemSize;

	}

	public RooflinePoint(long problemSize,
			Iterable<OperationalIntensity> operationalIntensities,
			Iterable<Performance> performances) {
		this(problemSize);
		addAll(this.operationalIntensities, operationalIntensities);
		addAll(this.performances, performances);
	}

	public RooflinePoint(long problemSize, OperationalIntensity opInt,
			Performance perf) {
		this(problemSize);
		addResult(opInt, perf);
	}

	public long getProblemSize() {
		return problemSize;
	}

	public void addResult(OperationalIntensity opInt, Performance perf) {
		operationalIntensities.add(opInt);
		performances.add(perf);
	}

	public DescriptiveStatistics getOperationalIntensityStats() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (OperationalIntensity op : operationalIntensities)
			stats.addValue(op.getValue());
		return stats;
	}

	public DescriptiveStatistics getPerformanceStats() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Performance perf : performances)
			stats.addValue(perf.getValue());
		return stats;
	}

	@Override
	public String toString() {
		return String.format("Point: [%s : %s]",
				getMedianOperationalIntensity().getValue(),
				getMedianPerformance().getValue());
	}

	public OperationalIntensity getMedianOperationalIntensity() {
		return new OperationalIntensity(getOperationalIntensityStats()
				.getPercentile(50));
	}

	public Performance getMedianPerformance() {
		return new Performance(getPerformanceStats().getPercentile(50));
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * merge this point with the given point. Problemm sizes have to be equal,
	 * label is concatenated, operational intensities and performances are
	 * merged
	 */
	public void merge(RooflinePoint point) {
		if (problemSize != point.getProblemSize())
			throw new Error("Problem sizes do not match");
		if (label == null)
			label = point.getLabel();
		else
			if (point.getLabel() != null)
				label += point.getLabel();
		addAll(operationalIntensities, point.operationalIntensities);
		addAll(performances, point.performances);
	}

	public int getN() {
		return operationalIntensities.size();
	}
}
