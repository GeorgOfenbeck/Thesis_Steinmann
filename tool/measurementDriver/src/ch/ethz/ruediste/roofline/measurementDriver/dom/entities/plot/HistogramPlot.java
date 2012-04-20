package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import java.util.*;

import org.apache.commons.lang3.Range;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

public class HistogramPlot extends Plot2D<HistogramPlot> {
	private final LinkedHashMap<String, Histogram> histograms = new LinkedHashMap<String, Histogram>();
	private final DescriptiveStatistics totalStats = new DescriptiveStatistics();

	private int binCount = 100;

	public HistogramPlot() {
	}

	public Map<String, Histogram> getHistograms() {
		return histograms;
	}

	public void addValue(String series, Double v) {
		if (!histograms.containsKey(series)) {
			histograms.put(series, new Histogram());
		}

		histograms.get(series).apply(v);
		totalStats.addValue(v);
	}

	public int getBinCount() {
		return binCount;
	}

	public HistogramPlot setBinCount(int binCount) {
		this.binCount = binCount;
		return This();
	}

	@Override
	public Range<Double> getXRange(SystemInformation systemInformation) {
		Range<Double> superRange = super.getXRange(systemInformation);
		if (totalStats.getN() == 0) {
			return superRange;
		}

		double min = superRange.getMinimum();
		double max = superRange.getMaximum();

		if (Double.isNaN(min)) {
			min = totalStats.getMin();
		}

		if (Double.isNaN(max)) {
			max = totalStats.getMax();
		}

		return Range.between(min, max);
	}
}
