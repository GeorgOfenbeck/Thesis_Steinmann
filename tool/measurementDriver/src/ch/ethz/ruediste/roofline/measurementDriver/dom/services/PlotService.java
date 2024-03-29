package ch.ethz.ruediste.roofline.measurementDriver.dom.services;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;
import static java.lang.Math.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.DistributionPlot.DistributionPlotSeries;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.PointPlot.Point;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.RooflinePlot.SameSizeConnection;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.SeriesPlot.SeriesPlotSeries;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.services.CommandService;
import ch.ethz.ruediste.roofline.measurementDriver.util.BinaryPredicates;
import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

import com.google.inject.Inject;

public class PlotService {
	public final static ConfigurationKey<Boolean> showMinMax = ConfigurationKey
			.Create(Boolean.class, "showMinMax",
					"show the minima and maxima in roofline plots",
					true);

	public final static ConfigurationKey<Boolean> showPercentiles = ConfigurationKey
			.Create(Boolean.class, "showPercentiles",
					"show the percentiles in roofline plots",
					true);
	
	public final static ConfigurationKey<KeyPosition> keyPositionKey = ConfigurationKey
			.Create(KeyPosition.class, "keyPosition",
					"Position of the key in the plots. Overrides any other setting",
					KeyPosition.Undefined);

	private final static Logger log = Logger.getLogger(PlotService.class);

	private static final double plotWidth = 14.337;
	private static final double plotHeight = plotWidth * 16. / 28.;

	private static double bMargin = 0.1;
	private static double tMargin = 0.93;
	private static double lMargin = 0.12;
	private static double rMargin = 0.95;

	private static int lwTic = 2;
	private static int lwMTic = 1;
	private static int lwMaxBound = 2;
	private static int lwBound = 2;
	private static int lwLine = 2;

	@Inject
	public CommandService commandService;

	@Inject
	public SystemInfoService systemInfoService;

	@Inject
	public Configuration configuration;

	static int pointTypes[] = { 5, 7, 9, 11, 13, };
	static String lineColors[] = { "black", "red", "green", "blue", "#FFFF00" };

	public void plot(HistogramPlot plot) throws ExecuteException, IOException {
		if (plot.getHistograms().size() == 0)
			return;
		// retrieve bin count
		int binCount = plot.getBinCount();

		List<Entry<String, Histogram>> histograms = toList(plot.getHistograms()
				.entrySet());

		Range<Double> range = plot.getXRange(systemInfoService
				.getSystemInformation());

		// write data
		PrintStream outputFile = new PrintStream(plot.getOutputName() + ".data");
		for (Entry<String, Histogram> entry : histograms) {
			// get histogram data
			int[] counts = entry.getValue().getCounts(binCount, range);
			double[] binCenters = entry.getValue().getBinCenters(binCount,
					range);

			for (int i = 0; i < binCount; i++) {
				outputFile.printf("%e\t%d\n", binCenters[i], counts[i]);
			}
			outputFile.printf("\n\n");
		}

		outputFile.close();

		// write gnuplot files
		PrintStream output = new PrintStream(plot.getOutputName()
				+ ".gnuplot");
		preparePlot(output, plot);

		// generate plot command
		List<String> plotLines = new ArrayList<String>();
		for (int histNr = 0; histNr < plot.getHistograms().size(); histNr++) {
			Entry<String, Histogram> hist = histograms.get(histNr);

			plotLines
					.add(String
							.format("'%s.data' index %d using 1:2 title '%s' with histeps lw %d lt -1 lc rgb\"%s\"",
									plot.getOutputName(), histNr,
									hist.getKey(),
									lwLine,
									getLineColor(histNr)));
		}

		output.println("plot \\");
		output.print(StringUtils.join(plotLines, ",\\\n"));
		output.close();

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { plot.getOutputName() + ".gnuplot" });
	}

	public void plot(SimplePlot plot) throws ExecuteException, IOException {
		if (isEmpty(plot.getValues()))
			return;

		// print data file
		final PrintStream outputFile = new PrintStream(plot.getOutputName()
				+ ".data");

		for (double value : plot.getValues()) {
			outputFile.printf("%e\n", value);
		}

		outputFile.close();

		// write gnuplot files
		{
			PrintStream output = new PrintStream(plot.getOutputName()
					+ ".gnuplot");
			preparePlot(output, plot);

			output.printf("plot '%s.data' notitle with linespoints \n",
					plot.getOutputName());
			// output.printf("pause mouse\n");

			output.close();
		}

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { plot.getOutputName() + ".gnuplot" });
	}

	private void preparePlot(PrintStream output, Plot<?> plot) {
		// set the output
		output.printf(
				"set terminal pdf color size %ecm,%ecm font 'Gill Sans, 4'\n",
				plotWidth, plotHeight);
		output.printf("set output '%s.pdf'\n", plot.getOutputName());

		// set the title
		output.printf("set title '%s' font 'Gill Sans, 8'\n", plot.getTitle());

		// disable border
		output.println("unset border");

		// set the point size
		output.println("set pointsize 0.25");

		// add gray background
		output.println("set object 1 rectangle from graph 0,0 to graph 1,1 behind fillcolor rgb\"#E0E0E0\" lw 0");

		// add white grid
		output.printf(
				"set grid xtics ytics mxtics mytics lt -1 lw %d linecolor rgb\"#FFFFFF\",lt -1 lw %d linecolor rgb\"#FFFFFF\"\n",
				lwTic, lwMTic);

		// set the margins
		output.printf("set bmargin at screen %e\n", bMargin);
		output.printf("set tmargin at screen %e\n", tMargin);
		output.printf("set lmargin at screen %e\n", lMargin);
		output.printf("set rmargin at screen %e\n", rMargin);
	}

	private void preparePlot(PrintStream output, Plot2D<?> plot) {
		preparePlot(output, (Plot<?>) plot);

		// place the legend
		KeyPosition keyPosition = plot.getKeyPosition();
		if (configuration.get(keyPositionKey)!=KeyPosition.Undefined)
			keyPosition=configuration.get(keyPositionKey);
		
		switch (keyPosition) {
		case BottomLeft:
			output.println("set key left bottom");
		break;
		case BottomRight:
			output.println("set key right bottom");
		break;
		case TopLeft:
			output.println("set key left top");
		break;
		case TopRight:
			output.println("set key right top");
		break;

		case NoKey:
			output.println("unset key");
		break;
		}

		// set logarithmic axes
		if (plot.isLogX()) {
			output.println("set log x");
		}

		if (plot.isLogY()) {
			output.println("set log y");
		}

		// set the scaling
		SystemInformation systemInformation = systemInfoService
				.getSystemInformation();
		Double xMin = (Double) plot.getXRange(systemInformation).getMinimum();
		Double xMax = (Double) plot.getXRange(systemInformation).getMaximum();
		Double yMin = (Double) plot.getYRange(systemInformation).getMinimum();
		Double yMax = (Double) plot.getYRange(systemInformation).getMaximum();

		output.printf("set xrange [%s:%s]\n",
				xMin.isInfinite() ? "" : xMin,
				xMax.isInfinite() ? "" : xMax);
		output.printf("set yrange [%s:%s]\n",
				yMin.isInfinite() ? "" : yMin,
				yMax.isInfinite() ? "" : yMax);

		output.printf("set xlabel '%s [%s]' font 'Gill Sans, 6'\n",
				plot.getxLabel(), plot.getxUnit());
		output.printf("set ylabel '%s [%s]' font 'Gill Sans, 6'\n",
				plot.getyLabel(), plot.getyUnit());
	}

	public void plot(DistributionPlot plot) throws ExecuteException,
			IOException {

		DescriptiveStatistics valueStats = plot.getValueStats();
		double min = valueStats.getMin();
		double max = valueStats.getMax();

		log.debug(String
				.format("entering plot(DistributionPlot) for plot %s. output name is %s",
						plot.getTitle(), plot.getOutputName()));
		List<DistributionPlotSeries> allSeries = plot.getAllSeries();

		// print data file
		final PrintStream outputFile = new PrintStream(plot.getOutputName()
				+ ".data");

		for (DistributionPlotSeries series : allSeries) {
			for (Entry<Long, DescriptiveStatistics> entry : series
					.getStatisticsMap().entrySet()) {

				DescriptiveStatistics statistics = entry.getValue();
				double perf = statistics.getPercentile(50);
				if (isNormal(perf))
					outputFile.printf("%d %e %e %e %e %e\n", entry.getKey(),
							getOr(statistics.getPercentile(25), min / 1e10),
							getOr(statistics.getMin(), min / 1e10),
							getOr(statistics.getMax(), max * 1e10),
							getOr(statistics.getPercentile(75), max * 1e10),
							perf);
			}
			outputFile.printf("\n\n");
		}
		outputFile.close();

		// write gnuplot files
		{
			PrintStream output = new PrintStream(plot.getOutputName()
					+ ".gnuplot");
			preparePlot(output, plot);

			if (!Double.isNaN(plot.getBoxWidth())) {
				output.printf("set boxwidth %f\n", plot.getBoxWidth());
			}

			List<String> plotLines = new ArrayList<String>();

			for (int seriesNr = 0; seriesNr < allSeries.size(); seriesNr++) {
				DistributionPlotSeries series = allSeries.get(seriesNr);

				if (series.maxN() > 1) {
					plotLines
							.add(String
									.format("'%s.data' index %d using 1:2:3:4:5 notitle  with candlesticks whiskerbars lw %d lc rgb\"%s\"",
											plot.getOutputName(), seriesNr,
											lwLine,
											getLineColor(seriesNr)));
				}
				plotLines
						.add(String
								.format("'%s.data' index %d using 1:6 title '%s' with linespoints lw %d lt -1 pt %d lc rgb\"%s\"",
										plot.getOutputName(), seriesNr,
										series.getName(),
										lwLine,
										getPointType(seriesNr),
										getLineColor(seriesNr)));
			}

			output.println("plot \\");
			output.print(StringUtils.join(plotLines, ",\\\n"));
			output.close();
		}

		// show output
		try {
			commandService.runCommand(new File("."), "gnuplot",
					new String[] { plot.getOutputName() + ".gnuplot" });
		}
		catch (Throwable e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void plot(SeriesPlot plot) throws ExecuteException, IOException {

		List<SeriesPlotSeries> allSeries = plot.getAllSeries();

		// print data file
		final PrintStream outputFile = new PrintStream(plot.getOutputName()
				+ ".data");

		for (SeriesPlotSeries series : allSeries) {
			for (Entry<Long, Double> entry : series.getStatisticsMap()
					.entrySet()) {

				outputFile.printf("%d %e\n", entry.getKey(), entry.getValue());
			}
			outputFile.printf("\n\n");
		}
		outputFile.close();

		// write gnuplot files
		{
			PrintStream output = new PrintStream(plot.getOutputName()
					+ ".gnuplot");
			preparePlot(output, plot);

			List<String> plotLines = new ArrayList<String>();

			for (int seriesNr = 0; seriesNr < allSeries.size(); seriesNr++) {
				SeriesPlotSeries series = allSeries.get(seriesNr);

				plotLines
						.add(String
								.format("'%s.data' index %d title '%s' with linespoints lw %d lt -1 pt %d lc rgb\"%s\"",
										plot.getOutputName(), seriesNr,
										series.getName(),
										lwLine,
										getPointType(seriesNr),
										getLineColor(seriesNr)));
			}

			output.println("plot \\");
			output.print(StringUtils.join(plotLines, ",\\\n"));
			output.close();
		}

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { plot.getOutputName() + ".gnuplot" });
	}

	boolean isNormal(double d) {
		return !Double.isInfinite(d) && !Double.isNaN(d);
	}

	double getOr(double d, double def) {
		if (isNormal(d))
			return d;
		return def;
	}

	/**
	 * plot a roofline plot
	 */
	public void plot(RooflinePlot plot) throws ExecuteException, IOException {
		SystemInformation systemInformation = systemInfoService
				.getSystemInformation();

		// print data file
		final PrintStream outputFile = new PrintStream(plot.getOutputName()
				+ ".data");

		double minX = plot.getXRange(systemInformation).getMinimum();
		double minY = plot.getYRange(systemInformation).getMinimum();
		double maxX = plot.getXRange(systemInformation).getMaximum();
		double maxY = plot.getYRange(systemInformation).getMaximum();

		{
			int i = 0;
			for (RooflineSeries serie : plot.getAllSeries()) {
				outputFile.printf("# [Median %d]\n", i);
				for (RooflinePoint point : serie.getPoints()) {
					double opInt = point.getMedianOperationalIntensity()
							.getValue();
					double perf = point.getMedianPerformance().getValue();
					outputFile.printf("%e %e\n",
							getOr(opInt, maxX * 2),
							getOr(perf, maxY * 2));
				}
				outputFile.printf("\n\n");
				i++;
			}
		}

		{
			int i = 0;
			for (RooflineSeries serie : plot.getAllSeries()) {
				outputFile.printf("# [Stats %d]\n", i);
				for (RooflinePoint point : serie.getPoints()) {
					if (point.getN() > 1) {
						DescriptiveStatistics opIntStats = point
								.getOperationalIntensityStats();

						DescriptiveStatistics perfStats = point
								.getPerformanceStats();

						double opInt = opIntStats.getPercentile(50);
						double perf = perfStats.getPercentile(50);
						if (isNormal(opInt) && isNormal(perf))
							outputFile
									.printf("%e %e %e %e %e %e %e %e %e %e \n",
											opInt,
											perf,
											getOr(opIntStats.getMin(), minX / 2),
											getOr(opIntStats.getMax(), maxX * 2),
											getOr(perfStats.getMin(), minY / 2),
											getOr(perfStats.getMax(), minY * 2),
											getOr(opIntStats.getPercentile(25),
													minX / 2),
											getOr(opIntStats.getPercentile(75),
													maxX * 2),
											getOr(perfStats.getPercentile(25),
													minY / 2),
											getOr(perfStats.getPercentile(75),
													maxY * 2));
					}
				}
				outputFile.printf("\n\n");
				i++;
			}
		}

		// print data for connecting points with same problem size
		{
			for (long problemSize : plot.getProblemSizes()) {
				// get all points with the problem size
				List<RooflinePoint> points = new ArrayList<RooflinePoint>();
				for (RooflineSeries serie : plot.getAllSeries()) {
					RooflinePoint point = serie.getPoint(problemSize);
					if (point != null)
						points.add(point);
				}

				// sort the points by operational intensity or performance
				switch (plot.getSameSizeConnection()) {
				case ByOperationalIntensity:
					Collections.sort(points,
							new Comparator<RooflinePoint>() {

								public int compare(RooflinePoint p1,
										RooflinePoint p2) {
									return Double.compare(p1
											.getMedianOperationalIntensity()
											.getValue(),
											p2.getMedianOperationalIntensity()
													.getValue());
								}
							});
				break;
				case ByPerformance:
					Collections.sort(points,
							new Comparator<RooflinePoint>() {

								public int compare(RooflinePoint p1,
										RooflinePoint p2) {
									return Double.compare(p1
											.getMedianPerformance()
											.getValue(),
											p2.getMedianPerformance()
													.getValue());
								}
							});
				break;
				case None:
				break;

				}

				// output the sorted points to the data file
				if (plot.getSameSizeConnection() != SameSizeConnection.None) {
					outputFile.printf("# [ProblemSize %d]\n", problemSize);
					for (RooflinePoint point : points)
					{
						double opInt = point.getMedianOperationalIntensity()
								.getValue();
						double perf = point.getMedianPerformance().getValue();
						if (isNormal(opInt) && isNormal(perf))
							outputFile.printf("%e %e\n",
									opInt,
									perf);
					}
					outputFile.printf("\n\n");
				}
			}
		}
		outputFile.close();

		// write gnuplot file
		{
			PrintStream output = new PrintStream(plot.getOutputName()
					+ ".gnuplot");
			preparePlot(output, plot);

			// add the ticks
			output.printf(
					"set xtics scale 0 (%s)\n",
					getLogTicks(plot.getXRange(systemInformation).getMinimum(),
							plot.getXRange(systemInformation)
									.getMaximum()));

			output.printf(
					"set ytics scale 0 (%s)\n",
					getLogTicks(plot.getYRange(systemInformation).getMinimum(),
							plot.getYRange(systemInformation)
									.getMaximum()));

			// build the peak performance lines
			List<String> plotLines = new ArrayList<String>();
			boolean first = true;
			for (Pair<String, Performance> peak : order(
					plot.getPeakPerformances(),
					BinaryPredicates
							.<String, Performance> pairRightComparator(Quantity
									.<Performance> moreThan()))) {
				// set the default color
				String lineColor = "rgb\"#B0B0B0\"";
				int lw = lwBound;
				if (first) {
					first = false;
					// set the color of the first line
					lineColor = "rgb\"black\"";
					lw = lwMaxBound;
				}

				// generate the string
				plotLines.add(String.format(
						"%e notitle with lines lc %s lw %d",
						peak.getRight().getValue(), lineColor, lw));
			}

			// print the lines for the peak performances
			for (Pair<String, Performance> peak : plot.getPeakPerformances()) {
				output.printf(
						"set label '%s (%.2g F/C)' at graph 1,first %e right offset -1,graph 0.015\n",
						peak.getLeft(), peak.getRight().getValue(), peak
								.getRight().getValue());
			}

			// build the peak bandwidth lines
			first = true;
			for (Pair<String, Throughput> peak : order(plot.getPeakBandwiths(),
					BinaryPredicates
							.<String, Throughput> pairRightComparator(Quantity
									.<Throughput> moreThan()))) {
				// set the default color
				String lineColor = "rgb\"#B0B0B0\"";
				int lw = lwBound;
				if (first) {
					first = false;
					// set the color of the first line
					lineColor = "rgb\"black\"";
					lw = lwMaxBound;
				}

				plotLines.add(String.format(
						"%e*x notitle with lines lc %s lw %d", peak.getRight()
								.getValue(), lineColor, lw));
			}

			// calculate the angle of the memory border lines
			double angle;
			{
				double aspectRatio = (double) plotHeight / (double) plotWidth;
				aspectRatio *= (tMargin - bMargin) / (rMargin - lMargin);

				double opIntensityRatio = plot.getXRange(systemInformation)
						.getMaximum()
						/ plot.getXRange(systemInformation).getMinimum();
				double performanceRatio = plot.getYRange(systemInformation)
						.getMaximum()
						/ plot.getYRange(systemInformation).getMinimum();

				angle = Math.toDegrees(Math.atan(aspectRatio
						* log(opIntensityRatio) / log(performanceRatio)));
			}

			// print the labels for the throughput borders
			for (Pair<String, Throughput> peak : plot.getPeakBandwiths()) {
				double performance = plot.getYRange(systemInformation)
						.getMaximum() * 0.85;
				double bandwidth = peak.getRight().getValue();
				double opIntens = plot.getXRange(systemInformation)
						.getMaximum();

				double borderOpIntens = performance / bandwidth;
				double borderPerformance = opIntens * bandwidth;

				if (borderOpIntens < opIntens) {
					// we hit the top of the graph
					output.printf(
							"set label '%s (%.2g B/C)' at first %e , first %e right offset 0,graph 0.02 rotate by %e\n",
							peak.getLeft(), bandwidth, borderOpIntens,
							performance, angle);
				}
				else {
					// we hit the right of the graph
					output.printf(
							"set label '%s (%.2g byte/cycle)' at graph 1, first %e right offset 0,graph -0.015 rotate by %e\n",
							peak.getLeft(), bandwidth, borderPerformance, angle);
				}

			}

			// build the points for the series
			List<RooflineSeries> allSeries = toList(plot.getAllSeries());
			for (int i = 0; i < allSeries.size(); i++) {
				RooflineSeries series = allSeries.get(i);

				plotLines
						.add(String
								.format("'%s.data' index '[Median %d]' title '%s' with linespoints lw %d lt -1 pt %d lc rgb\"%s\"",
										plot.getOutputName(), i,
										series.getName(), lwLine,
										getPointType(i),
										getLineColor(i)));

				if (configuration.get(showMinMax)
						&& series.anyPointWithMultipleValues()) {
					// plot min/max
					plotLines
							.add(String
									.format("'%s.data' index '[Stats %d]' using 1:2:3:4:5:6 notitle with xyerrorbars lw %d lt -1 lc rgb\"%s\"",
											plot.getOutputName(), i, lwLine,
											getLineColor(i)));
				}

				if (configuration.get(showPercentiles)
						&& series.anyPointWithMultipleValues()) {
					// plot 25/75 percentile
					plotLines
							.add(String
									.format("'%s.data' index '[Stats %d]' using 1:2:7:8:9:10 notitle with boxxyerrorbars lw %d lt -1 lc rgb\"%s\"",
											plot.getOutputName(), i, lwLine,
											getLineColor(i)));
				}

				// add label for the first and the last point
				{
					List<RooflinePoint> points = series.getPoints();
					for (int idx = 0; idx < points.size(); idx++)
						printLabel(output, points.get(idx), idx == 0
								|| idx == points.size() - 1);
				}
			}

			// connect the points of same problem size
			if (plot.getSameSizeConnection() != SameSizeConnection.None) {
				for (long problemSize : plot.getProblemSizes()) {
					plotLines
							.add(String
									.format("'%s.data' index '[ProblemSize %d]' notitle with lines lw %d lt 0 lc rgb\"#000000\"",
											plot.getOutputName(), problemSize,
											lwLine));
				}
			}

			output.println("plot \\");
			output.print(StringUtils.join(plotLines, ",\\\n"));

			output.close();
		}
		try {
			// show output
			commandService.runCommand(new File("."), "gnuplot",
					new String[] { plot.getOutputName() + ".gnuplot" });
		}
		catch (Throwable e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @param output
	 * @param point
	 */
	public void printLabel(PrintStream output, RooflinePoint point,
			boolean useProblemSizeIfNoLabel) {
		String label = point.getLabel();
		if (StringUtils.isEmpty(label) && useProblemSizeIfNoLabel)
			label = Long.toString(point.getProblemSize());
		if (!StringUtils.isEmpty(label)) {
			double opInt = point.getMedianOperationalIntensity().getValue();
			double perf = point.getMedianPerformance().getValue();
			if (isNormal(opInt) && isNormal(perf))
				output.printf(
						"set label \"%s\" at first %g,%g center nopoint offset graph 0,0.02 front\n",
						label, opInt,
						perf);
		}
	}

	private int getPointType(int index) {
		return pointTypes[getPointTypeIndex(index)];
	}

	public int getPointTypeIndex(int index) {
		int idx;
		if (index >= pointTypes.length) {
			// iterate slowly over point types
			idx = index - pointTypes.length;
			idx = idx / lineColors.length;
			idx = idx % pointTypes.length;
		}
		else
			idx = index;
		return idx;
	}

	private String getLineColor(int index) {
		if (index >= lineColors.length) {
			// iterate fast over line colors
			int idx = index - lineColors.length;
			idx = idx % (lineColors.length - 1);
			if (idx >= getPointTypeIndex(index))
				idx++;
			return lineColors[idx];
		}
		return lineColors[index];
	}

	/**
	 * @param min
	 * @param max
	 * @return
	 */
	public String getLogTicks(Double min, Double max) {
		ArrayList<String> ticks = new ArrayList<String>();

		// start with the magnitude of the minimum
		for (double magnitude = pow(10, floor(log10(min))); magnitude < max; magnitude *= 10) {
			// output a major tick if we are already larger than the minimum
			if (magnitude > min) {
				String label = StringUtils.strip(
						String.format("%f", magnitude), "0");
				// add a zero before the decimal point
				if (label.startsWith(".")) {
					label = "0" + label;
				}
				// strip a single dot at the end
				label = StringUtils.stripEnd(label, ".");

				ticks.add(String.format("\"%s\" %g 0", label, magnitude));
			}
			// iterate over the minor ticks
			for (int i = 1; i < 9; i++) {
				double d = magnitude + i * magnitude;
				// check if we are in range already
				if (d < min) {
					continue;
				}
				// break the loop if we are past the range
				if (d >= max) {
					break;
				}

				// add minor tick
				ticks.add(String.format("\"\" %g 1", d));
			}
		}

		String tickString = StringUtils.join(ticks, ",");
		return tickString;
	}

	public void plot(PointPlot plot) throws ExecuteException, IOException {
		if (plot.series.size() == 0)
			return;

		// write data
		PrintStream outputFile = new PrintStream(plot.getOutputName() + ".data");
		for (Entry<String, LinkedList<Point>> entry : plot.series.entrySet()) {
			// get histogram data
			for (Point p : entry.getValue()) {
				outputFile.printf("%e\t%e\n", p.x, p.y);
			}
			outputFile.printf("\n\n");
		}

		outputFile.close();

		// write gnuplot files
		PrintStream output = new PrintStream(plot.getOutputName()
				+ ".gnuplot");
		preparePlot(output, plot);

		List<Entry<String, LinkedList<Point>>> series = toList(plot.series
				.entrySet());

		// generate plot command
		List<String> plotLines = new ArrayList<String>();

		for (int seriesNr = 0; seriesNr < series.size(); seriesNr++) {
			Entry<String, LinkedList<Point>> ser = series.get(seriesNr);

			plotLines
					.add(String
							.format("'%s.data' index %d using 1:2 title '%s' with points pt %s lc rgb\"%s\"",
									plot.getOutputName(), seriesNr,
									ser.getKey(),
									getPointType(seriesNr),
									getLineColor(seriesNr)));
		}

		output.println("plot \\");
		output.print(StringUtils.join(plotLines, ",\\\n"));
		output.close();

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { plot.getOutputName() + ".gnuplot" });
	}
}
