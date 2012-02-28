package ch.ethz.ruediste.roofline.measurementDriver.services;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;
import static java.lang.Math.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.DistributionPlot.DistributionPlotSeries;
import ch.ethz.ruediste.roofline.measurementDriver.dom.SeriesPlot.SeriesPlotSeries;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.util.BinaryPredicates;

import com.google.inject.Inject;

public class PlotService {
	private final static Logger log = Logger.getLogger(PlotService.class);

	@Inject
	public CommandService commandService;

	static int pointTypes[] = { 5, 7, 9, 11, 13, };
	static String lineColors[] = { "black", "red", "green", "blue", "#FFFF00" };

	public void plot(HistogramPlot plot) throws ExecuteException, IOException {
		Histogram hist = plot.getHistogram();

		// retrieve histogram data
		int binCount = 200;
		int[] counts = hist.getCounts(binCount);
		double[] binCenters = hist.getBinCenters(binCount);

		// write data
		PrintStream outputFile = new PrintStream(plot.getOutputName() + ".data");
		for (int i = 0; i < binCount; i++) {
			outputFile.printf("%e\t%d\n", binCenters[i], counts[i]);
		}
		outputFile.close();

		// write gnuplot files
		{
			PrintStream output = new PrintStream(plot.getOutputName()
					+ ".gnuplot");
			output.printf("set title '%s'\n", plot.getTitle());
			output.printf("set terminal postscript color\n");
			output.printf("set output '%s.ps'\n", plot.getOutputName());
			output.printf("plot '%s.data' using 1:2 with histeps\n",
					plot.getOutputName());
			// output.printf("pause mouse\n");

			output.close();
		}

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { plot.getOutputName() + ".gnuplot" });
	}

	public void plot(SimplePlot plot) throws ExecuteException, IOException {
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
			output.printf("set title '%s'\n", plot.getTitle());
			output.printf("set terminal postscript color\n");
			output.printf("set output '%s.ps'\n", plot.getOutputName());

			output.printf("plot '%s.data' with points \n", plot.getOutputName());
			// output.printf("pause mouse\n");

			output.close();
		}

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { plot.getOutputName() + ".gnuplot" });
	}

	private void preparePlot(PrintStream output, Plot plot) {
		// set the output
		output.printf("set terminal pdf color size 28cm,18cm \n");
		output.printf("set output '%s.pdf'\n", plot.getOutputName());

		// set the title
		output.printf("set title '%s' font 'Gill Sans, 16'\n", plot.getTitle());

		// disable border
		output.println("unset border");

		// set the point size
		output.println("set pointsize 0.5");

		// add gray background
		output.println("set object 1 rectangle from graph 0,0 to graph 1,1 behind fillcolor rgb\"#E0E0E0\" lw 0");

		// add white grid
		output.println("set grid xtics ytics mxtics mytics lt -1 lw 6 linecolor rgb\"#FFFFFF\",lt -1 lw 2 linecolor rgb\"#FFFFFF\"");

	}

	private void preparePlot(PrintStream output, Plot2D plot) {
		preparePlot(output, (Plot) plot);

		// place the legend
		output.println("set key outside right top");

		// set logarithmic axes
		if (plot.isLogX())
			output.println("set log x");

		if (plot.isLogY())
			output.println("set log y");

		// set the scaling
		Double xMin = (Double) plot.getXRange().getMinimum();
		Double xMax = (Double) plot.getXRange().getMaximum();
		Double yMin = (Double) plot.getYRange().getMinimum();
		Double yMax = (Double) plot.getYRange().getMaximum();

		output.printf("set xrange [%s:%s]\n",
				xMin == Double.NEGATIVE_INFINITY ? "" : xMin,
				xMax == Double.POSITIVE_INFINITY ? "" : xMax);
		output.printf("set yrange [%s:%s]\n",
				yMin == Double.NEGATIVE_INFINITY ? "" : yMin,
				yMax == Double.POSITIVE_INFINITY ? "" : yMax);

		output.printf("set xlabel '%s [%s]' font 'Gill Sans'\n",
				plot.getxLabel(), plot.getxUnit());
		output.printf("set ylabel '%s [%s]' font 'Gill Sans'\n",
				plot.getyLabel(), plot.getyUnit());
	}

	public void plot(DistributionPlot plot) throws ExecuteException,
			IOException {

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
				outputFile.printf("%d %e %e %e %e\n", entry.getKey(),
						statistics.getPercentile(25), statistics.getMin(),
						statistics.getMax(), statistics.getPercentile(75));
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
				DistributionPlotSeries series = allSeries.get(seriesNr);

				plotLines
						.add(String
								.format("'%s.data' index %d title '%s' with candlesticks whiskerbars",
										plot.getOutputName(), seriesNr,
										series.getName()));
			}

			output.println("plot \\");
			output.print(StringUtils.join(plotLines, ",\\\n"));
			output.close();
		}

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { plot.getOutputName() + ".gnuplot" });
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

				plotLines.add(String.format(
						"'%s.data' index %d title '%s' with linespoints",
						plot.getOutputName(), seriesNr, series.getName()));
			}

			output.println("plot \\");
			output.print(StringUtils.join(plotLines, ",\\\n"));
			output.close();
		}

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { plot.getOutputName() + ".gnuplot" });
	}

	/**
	 * plot a roofline plot
	 */
	public void plot(RooflinePlot plot) throws ExecuteException, IOException {
		// print data file
		final PrintStream outputFile = new PrintStream(plot.getOutputName()
				+ ".data");

		for (RooflineSeries serie : plot.getAllSeries()) {
			for (RooflinePoint point : serie.getPoints()) {
				outputFile.printf("%e %e\n", point.getOperationalIntensity()
						.getValue(), point.getPerformance().getValue());
			}
			outputFile.printf("\n\n");
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
					getLogTicks(plot.getXRange().getMinimum(), plot.getXRange()
							.getMaximum()));

			output.printf(
					"set ytics scale 0 (%s)\n",
					getLogTicks(plot.getYRange().getMinimum(), plot.getYRange()
							.getMaximum()));

			// build the plot lines
			List<String> plotLines = new ArrayList<String>();
			boolean first = true;
			for (Pair<String, Performance> peak : order(
					plot.getPeakPerformances(),
					BinaryPredicates
							.<String, Performance> pairRightComparator(Quantity
									.<Performance> moreThan()))) {
				// set the default color
				String lineColor = "rgb\"#B0B0B0\"";
				if (first) {
					first = false;
					// set the color of the first line
					lineColor = "rgb\"black\"";
				}

				// generate the string
				plotLines
						.add(String
								.format("%e title '%s (%.2g flop/cycle)' with lines lc %s lw 6",
										peak.getRight().getValue(), peak
												.getLeft(), peak.getRight()
												.getValue(), lineColor));
			}

			first = true;
			for (Pair<String, Throughput> peak : order(plot.getPeakBandwiths(),
					BinaryPredicates
							.<String, Throughput> pairRightComparator(Quantity
									.<Throughput> moreThan()))) {
				// set the default color
				String lineColor = "rgb\"#B0B0B0\"";
				if (first) {
					first = false;
					// set the color of the first line
					lineColor = "rgb\"black\"";
				}

				plotLines
						.add(String
								.format("%e*x title '%s (%.2g byte/cycle)' with lines lc %s lw 6",
										peak.getRight().getValue(), peak
												.getLeft(), peak.getRight()
												.getValue(), lineColor));
			}

			List<RooflineSeries> allSeries = toList(plot.getAllSeries());
			for (int i = 0; i < allSeries.size(); i++) {
				RooflineSeries series = allSeries.get(i);

				plotLines
						.add(String
								.format("'%s.data' index %d title '%s' with linespoints lw 4 lt -1 pt %d lc rgb\"%s\"",
										plot.getOutputName(), i,
										series.getName(), getPointType(i),
										getLineColor(i)));

				// add label for the first and the last point
				printLabel(output, first(series.getPoints()));
				printLabel(output, last(series.getPoints()));
			}

			output.println("plot \\");
			output.print(StringUtils.join(plotLines, ",\\\n"));
			output.close();
		}

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { plot.getOutputName() + ".gnuplot" });
	}

	/**
	 * @param output
	 * @param point
	 */
	public void printLabel(PrintStream output, RooflinePoint point) {
		output.printf(
				"set label \"%s\" at first %g,%g center nopoint offset graph 0,0.02\n",
				point.getLabel(), point.getOperationalIntensity().getValue(),
				point.getPerformance().getValue());
	}

	private int getPointType(int index) {
		if (index >= pointTypes.length)
			throw new Error("too many series, add more point types");
		return pointTypes[index];
	}

	private String getLineColor(int index) {
		if (index >= lineColors.length)
			throw new Error("too many series, add more line colors");
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
				if (d < min)
					continue;
				// break the loop if we are past the range
				if (d >= max)
					break;

				// add minor tick
				ticks.add(String.format("\"\" %g 1", d));
			}
		}

		String tickString = StringUtils.join(ticks, ",");
		return tickString;
	}
}
