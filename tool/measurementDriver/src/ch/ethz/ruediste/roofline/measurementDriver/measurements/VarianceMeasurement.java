package ch.ethz.ruediste.roofline.measurementDriver.measurements;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.ExecutionTimeMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.ExecutionTimeMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.KBestMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.MeasurementSchemeDescriptionBase;
import ch.ethz.ruediste.roofline.dom.MeasurerDescriptionBase;
import ch.ethz.ruediste.roofline.dom.MemoryLoadKernelDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;
import ch.ethz.ruediste.roofline.measurementDriver.services.CommandService;

import com.google.inject.Inject;

public class VarianceMeasurement implements IMeasurement {

	@Inject
	public MeasurementAppController measurementAppController;

	@Inject
	public CommandService commandService;

	private static class Combination {
		public MeasurementSchemeDescriptionBase scheme;
		public MeasurerDescriptionBase measurer;
		PrintStream output;
	}

	public void measure(String outputName) throws IOException {
		// create schemes
		KBestMeasurementSchemeDescription kBestScheme = new KBestMeasurementSchemeDescription();
		kBestScheme.setWarmCaches(true);
		SimpleMeasurementSchemeDescription simpleScheme = new SimpleMeasurementSchemeDescription();
		simpleScheme.setWarmCaches(true);

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

		LinkedList<Combination> combinations = new LinkedList<VarianceMeasurement.Combination>();

		{
			Combination combination = new Combination();
			combination.scheme = kBestScheme;
			combination.measurer = perfEventMeasurer;
			combination.output = new PrintStream(outputName
					+ "perfBest.data");
			combinations.add(combination);
		}
		{
			Combination combination = new Combination();
			combination.scheme = kBestScheme;
			combination.measurer = timeMeasurer;
			combination.output = new PrintStream(outputName
					+ "timeBest.data");
			combinations.add(combination);
		}
		{
			Combination combination = new Combination();
			combination.scheme = simpleScheme;
			combination.measurer = perfEventMeasurer;
			combination.output = new PrintStream(outputName
					+ "perfSimple.data");
			combinations.add(combination);
		}
		{
			Combination combination = new Combination();
			combination.scheme = simpleScheme;
			combination.measurer = timeMeasurer;
			combination.output = new PrintStream(outputName
					+ "timeSimple.data");
			combinations.add(combination);
		}

		for (long blockSize = 1; blockSize < 1e6; blockSize = blockSize << 1) {
			// set block size
			kernel.setBlockSize(blockSize);

			for (Combination combination : combinations) {
				// wire measurement
				measurement.setScheme(combination.scheme);
				measurement.setMeasurer(combination.measurer);

				// perform measurement
				MeasurementResult result = measurementAppController.measure(
						measurement, 75);

				// create statistics
				DescriptiveStatistics statistics = null;
				if (combination.measurer instanceof PerfEventMeasurerDescription) {
					statistics = PerfEventMeasurerOutput.getStatistics(
							"cycles", result);
				}

				if (combination.measurer instanceof ExecutionTimeMeasurerDescription) {
					statistics = ExecutionTimeMeasurerOutput
							.getStatistics(result);
				}

				// append to output
				combination.output
						.printf("%d\t%e\t%e\t%e\t%e\t%e\n",
								blockSize,
								statistics.getMean(),
								statistics.getStandardDeviation(),
								statistics.getPercentile(50),
								statistics
										.getPercentile(50 - 68.2689492137 / 2),
								statistics
										.getPercentile(50 + 68.2689492137 / 2)
						);
			}
		}

		// close outputs
		for (Combination combination : combinations) {
			combination.output.close();
		}

		// write gnuplot files
		{
			PrintStream output = new PrintStream(outputName
					+ "stdev.gnuplot");
			output.printf("set title 'Mean/Stdev'\n");
			output.printf("set terminal postscript color\n");
			output.printf("set output '%s'\n", outputName + "stdev.ps");
			output.printf("set log xy\n");
			output.printf(
					"plot '%sperfBest.data' using 1:2:3 with yerrorbars,\\\n",
					outputName);
			output.printf(
					"'%sperfSimple.data' using 1:2:3 with yerrorbars,\\\n",
					outputName);
			output.printf("'%stimeBest.data' using 1:2:3 with yerrorbars,\\\n",
					outputName);
			output.printf("'%stimeSimple.data' using 1:2:3 with yerrorbars\n",
					outputName);
			// output.printf("pause mouse\n");

			output.close();
		}

		{
			PrintStream output = new PrintStream(outputName
					+ "percentiles.gnuplot");
			output.printf("set title 'Percentiles'\n");
			output.printf("set terminal postscript color\n");
			output.printf("set output '%s'\n", outputName + "percentiles.ps");
			output.printf("set log xy\n");
			output.printf(
					"plot '%sperfBest.data' using 1:4:5:6 with yerrorbars,\\\n",
					outputName);
			output.printf(
					"'%sperfSimple.data' using 1:4:5:6 with yerrorbars,\\\n",
					outputName);
			output.printf(
					"'%stimeBest.data' using 1:4:5:6 with yerrorbars,\\\n",
					outputName);
			output.printf(
					"'%stimeSimple.data' using 1:4:5:6 with yerrorbars\n",
					outputName);
			// output.printf("pause mouse\n");

			output.close();
		}

		// show output
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { outputName
						+ "stdev.gnuplot" });
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { outputName
						+ "percentiles.gnuplot" });
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
