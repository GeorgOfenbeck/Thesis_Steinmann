package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.toList;

import java.util.*;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class DistributionPlot extends Plot2D<DistributionPlot> {
	public static class DistributionPlotSeries {
		private final HashMap<Long, DescriptiveStatistics> statisticsMap = new LinkedHashMap<Long, DescriptiveStatistics>();
		private final String name;

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

	}

	private final HashMap<String, DistributionPlotSeries> allSeries = new LinkedHashMap<String, DistributionPlotSeries>();

	public void addValue(String seriesName, long x, double y) {
		getSeries(seriesName).addValue(x, y);
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

	public DescriptiveStatistics getStatisticsOfN() {
		DescriptiveStatistics result = new DescriptiveStatistics();
		for (DistributionPlotSeries series : getAllSeries()) {
			for (DescriptiveStatistics seriesStats : series.getStatisticsMap()
					.values()) {
				result.addValue(seriesStats.getN());
			}
		}
		return result;
	}

	@Override
	public String getTitle() {
		DescriptiveStatistics statisticsOfN = getStatisticsOfN();
		if (Math.abs(statisticsOfN.getMin() - statisticsOfN.getMax()) < 0.1) {
			return String.format("%s (n=%.0f)", super.getTitle(),
					statisticsOfN.getMax());
		}
		else {
			return String.format("%s (n=%.0f...%.0f)", super.getTitle(),
					statisticsOfN.getMin(), statisticsOfN.getMax());
		}
	}
}
