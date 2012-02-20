package ch.ethz.ruediste.roofline.measurementDriver.services;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;
import static java.lang.Math.*;

import java.io.*;
import java.util.*;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.util.BinaryPredicates;

import com.google.inject.Inject;

public class PlotService {
	@Inject
	public CommandService commandService;

	static int pointTypes[] = { 5, 7, 9, 11, 13, };

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
			output.printf("set title '%s' font 'Gill Sans, 16'\n",
					plot.getTitle());
			output.printf("set terminal pdf color size 28cm,18cm\n");
			output.printf("set output '%s.pdf'\n", plot.getOutputName());

			// disable border
			output.println("unset border");

			// add gray background
			output.println("set object 1 rectangle from graph 0,0 to graph 1,1 behind fillcolor rgb\"#E0E0E0\" lw 0");

			// add white grid
			output.println("set grid xtics ytics mxtics mytics lt -1 lw 6 linecolor rgb\"#FFFFFF\",lt -1 lw 2 linecolor rgb\"#FFFFFF\"");

			// add the ticks
			output.printf(
					"set xtics scale 0 (%s)\n",
					getTicks(plot.getXRange().getMinimum(), plot.getXRange()
							.getMaximum()));

			output.printf(
					"set ytics scale 0 (%s)\n",
					getTicks(plot.getYRange().getMinimum(), plot.getYRange()
							.getMaximum()));

			// blace the legend
			output.println("set key outside right top");

			// set logarithmic axes
			output.println("set log x");
			output.println("set log y");

			// set the scaling
			output.printf("set xrange [%g:%g]\n",
					plot.getXRange().getMinimum(), plot.getXRange()
							.getMaximum());
			output.printf("set yrange [%g:%g]\n",
					plot.getYRange().getMinimum(), plot.getYRange()
							.getMaximum());

			output.printf("set xlabel '%s [%s]' font 'Gill Sans'\n",
					plot.getxLabel(), plot.getxUnit());
			output.printf("set ylabel '%s [%s]' font 'Gill Sans'\n",
					plot.getyLabel(), plot.getyUnit());

			// build the plot lines
			List<String> plotLines = new ArrayList<String>();
			boolean first = true;
			for (Pair<String, Performance> peak : order(
					plot.getPeakPerformances(),
					BinaryPredicates
							.<String, Performance> pairRightComparator(Quantity
									.<Performance> moreThan()))) {
				// set the default color
				String lineColor = "rgb\"#808080\"";
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
				String lineColor = "rgb\"#808080\"";
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
								.format("'%s.data' index %d title '%s' with linespoints lw 6 lt -1 pt %d lc rgb\"black\"",
										plot.getOutputName(), i,
										series.getName(), getPointType(i)));

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
	 * @param firstPoint
	 */
	public void printLabel(PrintStream output, RooflinePoint firstPoint) {
		output.printf(
				"set label \"%s\" at first %g,%g center nopoint offset graph 0,0.02\n",
				firstPoint.getLabel(), firstPoint.getOperationalIntensity()
						.getValue(), firstPoint.getPerformance().getValue());
	}

	int getPointType(int index) {
		if (index >= pointTypes.length)
			throw new Error("too many series, add more point types");
		return pointTypes[index];
	}

	/**
	 * @param min
	 * @param max
	 * @return
	 */
	public String getTicks(Double min, Double max) {
		ArrayList<String> ticks = new ArrayList<String>();

		// start with the magnitude of the minimum
		for (double magnitude = pow(10, floor(log10(min))); magnitude < max; magnitude *= 10) {
			// output a major tick if we are already larger than the minimum
			if (magnitude > min) {
				ticks.add(String.format("\"%.0f\" %g 0", magnitude, magnitude));
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
