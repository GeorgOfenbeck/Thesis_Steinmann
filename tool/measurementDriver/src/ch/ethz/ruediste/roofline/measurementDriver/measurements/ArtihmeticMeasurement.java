package ch.ethz.ruediste.roofline.measurementDriver.measurements;

import java.io.IOException;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.ArithmeticKernelDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurement;

import com.google.inject.Inject;

public class ArtihmeticMeasurement implements IMeasurement {

	public String getName() {
		return "arithmetic";
	}

	public String getDescription() {
		return "performs all arithmetic measurements";
	}

	@Inject
	public MeasurementAppController measurementAppController;

	public void measure(String outputName) throws IOException {
		measure(outputName, 10000);
		measure(outputName, 100000);
	}

	private void measure(String outputName, int iterations) {
		measure(outputName, iterations, "ArithmeticOperation_ADD");
		measure(outputName, iterations, "ArithmeticOperation_MUL");

	}

	private void measure(String outputName, int iterations, String operation) {
		measure(outputName, iterations, operation, false);
		measure(outputName, iterations, operation, true);
	}

	private void measure(String outputName, int iterations, String operation,
			boolean use_sse) {
		measure(outputName, iterations, operation, use_sse, "Unroll_None");
		measure(outputName, iterations, operation, use_sse, "Unroll_2");
		measure(outputName, iterations, operation, use_sse, "Unroll_4");

	}

	private void measure(String outputName, int iterations, String operation,
			boolean use_sse, String unroll) {

		ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
		kernel.setIterations(iterations);

		PerfEventMeasurerDescription measurer = new PerfEventMeasurerDescription();
		measurer.addEvent("cycles", "perf::PERF_COUNT_HW_CPU_CYCLES");

		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(kernel);
		measurement.setScheme(new SimpleMeasurementSchemeDescription());
		measurement.setMeasurer(measurer);

		if (use_sse) {
			measurement.setOptimization("-O3 -msse2");
		}
		else {
			measurement.setOptimization("-O3");
		}

		measurement.addMacro(ArithmeticKernelDescription.operationMacroName,
				operation);
		measurement.addMacro(ArithmeticKernelDescription.unrollMacroName,
				unroll);

		MeasurementResult result = measurementAppController.measure(
				measurement, 10);
		DescriptiveStatistics statistics = PerfEventMeasurerOutput
				.getStatistics("cycles", result);

		System.out.printf("%s %s %s %s: %g %g\n", operation, iterations,
				unroll, use_sse ? "SSE" : "NoSSE",
				statistics.getMin(),
				statistics.getPercentile(50) / statistics.getMin());
	}
}
