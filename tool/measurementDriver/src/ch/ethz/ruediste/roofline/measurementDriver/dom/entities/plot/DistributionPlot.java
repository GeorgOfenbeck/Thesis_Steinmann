package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.toList;

import java.util.*;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class DistributionPlot extends Plot2D<DistributionPlot> {
	public static class DistributionPlotSeries {
		private final HashMap<Long, DescriptiveStatistics> statisticsMap = new LinkedHashMap<Long, DescriptiveStatistics>();
		private final String name;
		public Object getStatisticsMap;

		public DistributionPlotSeries(String name) {
			super();
			this.name = name;
		}

		private DescriptiveStatistics getStatistics(long x) {
			if (!getStatisticsMap().containsKey(x)) {
				getStatisticsMap().put(x, new DescriptiveStatistics());
			}
			return getStatisticsMap().get(x);
		}

		public HashMap<Long, DescriptiveStatistics> getStatisticsMap() {
			return statisticsMap;
		}

		public void addValue(long x, double y) {
			getStatistics(x).addValue(y);
		}

		public String getName() {
			return name;
		}

		public void addValues(long x, DescriptiveStatistics values) {
			DescriptiveStatistics stats = getStatistics(x);
			for (double d : values.getValues()) {
				stats.addValue(d);
			}
		}

		public long maxN() {
			long max = 0;
			for (DescriptiveStatistics s : statisticsMap.values())
				max = Math.max(s.getN(), max);
			return max;
		}
	}

	private final HashMap<String, DistributionPlotSeries> allSeries = new LinkedHashMap<String, DistributionPlotSeries>();

	private double boxWidth = Double.NaN;

	public void addValue(String seriesName, long x, double y) {
		getSeries(seriesName).addValue(x, y);
	}

	public void addValues(String seriesName, long x,
			DescriptiveStatistics values) {
		getSeries(seriesName).addValues(x, values);
	}

	private DistributionPlotSeries getSeries(String seriesName) {
		if (!allSeries.containsKey(seriesName)) {
			allSeries.put(seriesName, new DistributionPlotSeries(seriesName));
		}
		return allSeries.get(seriesName);
	}

	public List<DistributionPlotSeries> getAllSeries() {
		return toList(allSeries.values());
	}

	public void addSeries(DistributionPlotSeries series) {
		allSeries.put(series.getName(), series);
	}

	public void addSeries(Iterable<DistributionPlotSeries> series) {
		for (DistributionPlotSeries s : series)
			allSeries.put(s.getName(), s);
	}

	private DescriptiveStatistics getStatisticsOfN() {
		DescriptiveStatistics result = new DescriptiveStatistics();
		for (DistributionPlotSeries series : getAllSeries()) {
			for (DescriptiveStatistics seriesStats : series.getStatisticsMap()
					.values()) {
				long n = seriesStats.getN();
				if (n > 1)
					result.addValue(n);
			}
		}
		return result;
	}

	@Override
	public String getTitle() {
		DescriptiveStatistics statisticsOfN = getStatisticsOfN();
		if (statisticsOfN.getN() == 0)
			return super.getTitle();

		if (Math.abs(statisticsOfN.getMin() - statisticsOfN.getMax()) < 0.1) {
			return String.format("%s (n=%.0f)", super.getTitle(),
					statisticsOfN.getMax());
		}
		else {
			return String.format("%s (n=%.0f...%.0f)", super.getTitle(),
					statisticsOfN.getMin(), statisticsOfN.getMax());
		}
	}

	public double getBoxWidth() {
		if (Double.isNaN(boxWidth) && isLogX())
			return 0.1;
		return boxWidth;
	}

	public DistributionPlot setBoxWidth(double boxWidth) {
		this.boxWidth = boxWidth;
		return This();
	}

	public DescriptiveStatistics getValueStats() {

		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (DistributionPlotSeries serie : getAllSeries()) {
			for (DescriptiveStatistics entry : serie.getStatisticsMap()
					.values()) {
				for (double v : entry.getValues()) {
					if (!Double.isInfinite(v) && !Double.isNaN(v))
						stats.addValue(v);
				}
			}
		}
		return stats;
	}
}
