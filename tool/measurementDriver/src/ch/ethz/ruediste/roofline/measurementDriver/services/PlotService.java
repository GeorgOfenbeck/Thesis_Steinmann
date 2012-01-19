package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.io.*;
import java.util.*;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;

import com.google.inject.Inject;

public class PlotService {
	@Inject
	public CommandService commandService;

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
			output.printf(
					"plot '%s.data' using 1:2 with histeps\n",
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

			output.printf(
					"plot '%s.data' with points \n",
					plot.getOutputName());
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

		for (RooflinePoint point : plot.getPoints()) {
			outputFile.printf("%e %e\n\n\n", point.getOperationalIntensity(),
					point.getPerformance());
		}

		outputFile.close();

		// write gnuplot file
		{
			PrintStream output = new PrintStream(plot.getOutputName()
					+ ".gnuplot");
			output.printf("set title '%s'\n", plot.getTitle());
			output.printf("set terminal postscript color\n");
			output.printf("set output '%s.ps'\n", plot.getOutputName());

			output.println("set log x");
			output.println("set log y");
			output.println("set xrange [0.01:10]");
			output.printf("set xlabel '%s [%s]'\n", plot.getxLabel(),
					plot.getxUnit());
			output.printf("set ylabel '%s [%s]'\n", plot.getyLabel(),
					plot.getyUnit());

			output.println("plot \\");

			List<String> plotLines = new ArrayList<String>();
			for (Pair<String, Performance> peak : plot.getPeakPerformances()) {
				plotLines.add(String.format("%e title '%s'",
						peak.getRight().getValue(), peak.getLeft()));
			}

			for (Pair<String, Throughput> peak : plot.getPeakBandwiths()) {
				plotLines.add(String.format("%e*x title '%s (%g bytes/cycle)'",
						peak.getRight().getValue(), peak.getLeft(),
						peak.getRight().getValue()));
			}

			for (int i = 0; i < plot.getPoints().size(); i++) {
				RooflinePoint point = plot.getPoints().get(i);

				plotLines.add(String.format(
						"'%s.data' index %d title '%s' with points \n",
						plot.getOutputName(), i, point.getName()));
			}

			output.print(StringUtils.join(plotLines, ",\\\n"));
			output.close();
		}

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { plot.getOutputName() + ".gnuplot" });
	}
}
