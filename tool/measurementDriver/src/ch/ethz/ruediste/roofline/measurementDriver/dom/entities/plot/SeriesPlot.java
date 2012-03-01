package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.toList;

import java.util.*;


public class SeriesPlot extends Plot2D<SeriesPlot> {
	public static class SeriesPlotSeries {
		private final HashMap<Long, Double> valueMap = new LinkedHashMap<Long, Double>();
		private final String name;

		public SeriesPlotSeries(String name) {
			super();
			this.name = name;
		}

		public HashMap<Long, Double> getStatisticsMap() {
			return valueMap;
		}

		public void setValue(long x, double y) {
			valueMap.put(x, y);
		}

		public String getName() {
			return name;
		}

	}

	private final HashMap<String, SeriesPlotSeries> allSeries = new LinkedHashMap<String, SeriesPlotSeries>();

	public void setValue(String seriesName, long x, double y) {
		getSeries(seriesName).setValue(x, y);
	}

	private SeriesPlotSeries getSeries(String seriesName) {
		if (!allSeries.containsKey(seriesName)) {
			allSeries.put(seriesName, new SeriesPlotSeries(seriesName));
		}
		return allSeries.get(seriesName);
	}

	public List<SeriesPlotSeries> getAllSeries() {
		return toList(allSeries.values());
	}

}
