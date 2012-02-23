package ch.ethz.ruediste.roofline.measurementDriver.dom;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.toList;

import java.util.*;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class DistributionPlot extends Plot2D {
	public static class DistributionPlotSeries {
		private final HashMap<Long, DescriptiveStatistics> statisticsMap = new HashMap<Long, DescriptiveStatistics>();
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

	private final HashMap<String, DistributionPlotSeries> allSeries = new HashMap<String, DistributionPlotSeries>();

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

}
