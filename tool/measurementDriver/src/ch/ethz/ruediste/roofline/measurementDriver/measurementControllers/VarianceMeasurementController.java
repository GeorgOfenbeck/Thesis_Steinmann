package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.MeasurementDescription.*;

import java.io.*;
import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;

import com.google.inject.Inject;

public class VarianceMeasurementController implements IMeasurementController {

	public String getName() {
		return "variance";
	}

	public String getDescription() {
		return "";
	}

	@Inject
	public MeasurementService measurementService;

	@Inject
	public CommandService commandService;

	public void measure(String outputName) throws IOException {
		ParameterSpace parameterSpace = new ParameterSpace();

		// create schemes
		KBestMeasurementSchemeDescription kBestScheme = new KBestMeasurementSchemeDescription();
		kBestScheme.setWarmCaches(true);
		parameterSpace.add(measurementSchemeAxis, kBestScheme);

		SimpleMeasurementSchemeDescription simpleScheme = new SimpleMeasurementSchemeDescription();
		simpleScheme.setWarmCaches(true);
		parameterSpace.add(measurementSchemeAxis, simpleScheme);

		// create kernel
		MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
		kernel.setOptimization("-O0");
		parameterSpace.add(kernelAxis, kernel);

		// create measurers
		PerfEventMeasurerDescription perfEventMeasurer = new PerfEventMeasurerDescription();
		perfEventMeasurer.addEvent("cycles", "perf::PERF_COUNT_HW_BUS_CYCLES");
		parameterSpace.add(measurerAxis, perfEventMeasurer);
		ExecutionTimeMeasurerDescription timeMeasurer = new ExecutionTimeMeasurerDescription();
		parameterSpace.add(measurerAxis, timeMeasurer);

		// add block sizes
		for (long blockSize = 1; blockSize < 1e4; blockSize = blockSize << 1) {
			parameterSpace.add(bufferSizeAxis, blockSize);
		}

		// create output streams
		HashMap<Coordinate, PrintStream> outputStreams = new HashMap<Coordinate, PrintStream>();
		for (Coordinate coordinate : parameterSpace.getProjection(
				measurementSchemeAxis, measurerAxis)) {

			String streamName = outputName;
			streamName += (coordinate.get(measurerAxis) == perfEventMeasurer ? "perf"
					: "time");
			streamName += (coordinate.get(measurementSchemeAxis) == kBestScheme ? "Best"
					: "Simple");

			outputStreams
					.put(coordinate, new PrintStream(streamName + ".data"));
		}

		for (Coordinate coordinate : parameterSpace) {
			MeasurementDescription measurement = new MeasurementDescription(
					coordinate);

			// perform measurement
			MeasurementResult result = measurementService.measure(measurement,
					10);

			// create statistics
			DescriptiveStatistics statistics = null;
			if (measurement.getMeasurer() instanceof PerfEventMeasurerDescription) {
				statistics = PerfEventMeasurerOutput.getStatistics("cycles",
						result);
			}

			if (measurement.getMeasurer() instanceof ExecutionTimeMeasurerDescription) {
				statistics = ExecutionTimeMeasurerOutput.getStatistics(result);
			}

			// append to output
			outputStreams.get(
					coordinate.getProjection(measurementSchemeAxis,
							measurerAxis)).printf("%d\t%e\t%e\t%e\t%e\t%e\n",
					coordinate.get(bufferSizeAxis), statistics.getMean(),
					statistics.getStandardDeviation(),
					statistics.getPercentile(50),
					statistics.getPercentile(50 - 68.2689492137 / 2),
					statistics.getPercentile(50 + 68.2689492137 / 2));
		}

		// close outputs
		for (PrintStream output : outputStreams.values()) {
			output.close();
		}

		// write gnuplot files
		{
			PrintStream output = new PrintStream(outputName + "stdev.gnuplot");
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
				new String[] { outputName + "stdev.gnuplot" });
		commandService.runCommand(new File("."), "gnuplot",
				new String[] { outputName + "percentiles.gnuplot" });
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
		System.out.println(summary.getStandardDeviation() / summary.getMean());
		System.out.print("median:");
		System.out.println(summary.getPercentile(50));
		System.out.print("min:");
		System.out.println(summary.getMin());
		System.out.print("max:");
		System.out.println(summary.getMax());
		System.out.println();
	}

}
