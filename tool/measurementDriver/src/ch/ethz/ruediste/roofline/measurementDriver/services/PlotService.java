package ch.ethz.ruediste.roofline.measurementDriver.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.exec.ExecuteException;

import ch.ethz.ruediste.roofline.measurementDriver.dom.Histogram;
import ch.ethz.ruediste.roofline.measurementDriver.dom.HistogramPlot;
import ch.ethz.ruediste.roofline.measurementDriver.dom.SimplePlot;

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
}
