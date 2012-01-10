package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.ArithmeticKernelDescription;
import ch.ethz.ruediste.roofline.dom.ArithmeticSingleKernelDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.MeasurementRepository;

import com.google.inject.Inject;

public class ArithmeticMeasurementController implements IMeasurementController {

	public String getName() {
		return "arithmetic";
	}

	public String getDescription() {
		return "performs all arithmetic measurements";
	}

	@Inject
	public MeasurementRepository measurementRepository;

	public void measure(String outputName) throws IOException {
		measure(outputName, 10000);
		measure(outputName, 100000);
	}

	private void measure(String outputName, int iterations) {
		measure(outputName, iterations, "ArithmeticOperation_ADD");
		measure(outputName, iterations, "ArithmeticOperation_MUL");
		measure(outputName, iterations, "ArithmeticOperation_MULADD");
	}

	private void measure(String outputName, int iterations, String operation) {
		measure(outputName, iterations, operation, false);
		measure(outputName, iterations, operation, true);
	}

	private void measure(String outputName, int iterations, String operation,
			boolean use_sse) {
		measure(outputName, iterations, operation, use_sse, 1);
		measure(outputName, iterations, operation, use_sse, 2);
		measure(outputName, iterations, operation, use_sse, 4);
		measure(outputName, iterations, operation, use_sse, 8);
		measure(outputName, iterations, operation, use_sse, 16);
		measure(outputName, iterations, operation, use_sse, 32);
		measure(outputName, iterations, operation, use_sse, 64);
	}

	private void measure(String outputName, int iterations, String operation,
			boolean use_sse, int unroll) {

		// ArithmeticKernelDescription kernel = new
		// ArithmeticKernelDescription();
		ArithmeticSingleKernelDescription kernel = new ArithmeticSingleKernelDescription();
		kernel.setIterations(iterations);
		kernel.setUnroll(unroll);

		PerfEventMeasurerDescription measurer = new
				PerfEventMeasurerDescription();
		measurer.addEvent("cycles",
				"coreduo::UNHALTED_CORE_CYCLES");
		// "coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:PACKED_DOUBLE");
		// "coreduo::FP_COMP_INSTR_RET");
		// "coreduo::INSTR_RET");
		// "coreduo::UNHALTED_REFERENCE_CYCLES");

		// ExecutionTimeMeasurerDescription measurer = new
		// ExecutionTimeMeasurerDescription();

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

		measurement.addMacro(ArithmeticKernelDescription.operationMacro,
				operation);

		MeasurementResult result = measurementRepository.getMeasurementResults(
				measurement, 10);
		DescriptiveStatistics statistics = PerfEventMeasurerOutput
				.getStatistics("cycles", result);
		// = ExecutionTimeMeasurerOutput.getStatistics(result);

		System.out.printf("%s %s %s %s: %g %g\n", operation, iterations,
				unroll, use_sse ? "SSE" : "NoSSE",
				statistics.getMin(),
				statistics.getPercentile(50) / statistics.getMin());
	}
}
