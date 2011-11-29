package ch.ethz.ruediste.roofline.measurementDriver.measurements;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.ExecutionTimeMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.ExecutionTimeMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.KBestMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.MemoryLoadKernelDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.services.CommandService;
import ch.ethz.ruediste.roofline.statistics.Histogram;

import com.google.inject.Inject;

public class VarianceHistogramMeasurement implements IMeasurement {

	@Inject
	public MeasurementAppController measurementAppController;

	@Inject
	public CommandService commandService;

	@Override
	public void measure(String outputName) throws IOException {
		// create schemes
		KBestMeasurementSchemeDescription kBestScheme = new KBestMeasurementSchemeDescription();
		SimpleMeasurementSchemeDescription simpleScheme = new SimpleMeasurementSchemeDescription();

		// create kernel
		MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();

		// create measurers
		PerfEventMeasurerDescription perfEventMeasurer = new PerfEventMeasurerDescription();
		perfEventMeasurer.addEvent("cycles", "perf::PERF_COUNT_HW_BUS_CYCLES");
		ExecutionTimeMeasurerDescription timeMeasurer = new ExecutionTimeMeasurerDescription();

		// measurement
		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setOptimization("-O0");
		measurement.setKernel(kernel);

		measurement.setScheme(kBestScheme);
		measurement.setMeasurer(perfEventMeasurer);
		kernel.setBlockSize(64);

		// perform measurement
		MeasurementResult result = measurementAppController.measure(
				measurement, 100);

		PrintStream outputFile = new PrintStream(outputName + ".data");

		// create statistics
		Histogram hist = new Histogram();
		if (measurement.getMeasurer() instanceof PerfEventMeasurerDescription) {
			PerfEventMeasurerOutput.addValues("cycles", result, hist);
		}

		if (measurement.getMeasurer() instanceof ExecutionTimeMeasurerDescription) {
			ExecutionTimeMeasurerOutput.addValues(result, hist);
		}

		int binCount = 100;
		int[] counts = hist.getCounts(binCount);
		String[] binLabels = hist.getBinLabels(binCount);
		int max = 0;
		for (int i = 0; i < binCount; i++) {
			outputFile.printf("%s\t%d\n", binLabels[i], counts[i]);
			max = Math.max(max, counts[i]);
		}

		outputFile.close();

		// write gnuplot files
		{
			PrintStream output = new PrintStream(outputName
					+ ".gnuplot");
			output.printf("set title '%d:%s'\n", kernel.getBlockSize(),
					measurement.toString());
			output.printf("set terminal postscript color\n");
			output.printf("set output '%s:%d:%s.ps'\n", outputName,
					kernel.getBlockSize(),
					measurement.toString());
			// output.printf("set xrange [-0.5:%d]\n", binCount);
			// output.printf("set yrange [0:%d]\n", max + 1);
			// output.printf("set style histogram rowstacked\n");
			/*
			 * output.printf( "plot '%s.data' using 2:xtic(1) with histogram\n",
			 * outputName);
			 */
			output.printf(
					"plot '%s.data' using 1:2 with histeps\n",
					outputName);
			// output.printf("pause mouse\n");

			output.close();
		}

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { outputName + ".gnuplot" });
	}

	static void printSummary(DescriptiveStatistics summary) {
		System.out.println("Measurement");
		System.out.print("number of outputs: ");
		System.out.println(summary.getN());
		System.out.print("mean:");
		System.out.println(summary.getMean());
		System.out.print("stddev:");
		System.out.println(summary.getStandardDeviation());
		System.out.print("relative:");
		System.out.println(summary.getStandardDeviation()
				/ summary.getMean());
		System.out.print("median:");
		System.out.println(summary.getPercentile(50));
		System.out.print("min:");
		System.out.println(summary.getMin());
		System.out.print("max:");
		System.out.println(summary.getMax());
		System.out.println();
	}

}
