package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.MeasurementDescription.*;

import java.io.*;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

import com.google.inject.Inject;

public class ArithmeticMeasurementController implements IMeasurementController {

	public String getName() {
		return "arithmetic";
	}

	public String getDescription() {
		return "performs all arithmetic measurements";
	}

	@Inject
	MeasurementAppController measurementAppController;

	public void measure(String outputName) throws IOException {
		ParameterSpace space = new ParameterSpace();
		space.add(iterationsAxis, 10000L);
		// space.add(iterationsAxis, 100000L);

		space.add(kernelAxis, new ArithmeticKernelDescription());
		// space.add(kernelAxis, new ArithmeticSingleKernelDescription());
		space.add(measurementSchemeAxis,
				new SimpleMeasurementSchemeDescription());

		{
			space.add(MeasurementDescription.measurerAxis,
					new PerfEventMeasurerDescription("cycles",
							"core::UNHALTED_CORE_CYCLES"
					// "coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:PACKED_DOUBLE"
					// "coreduo::FP_COMP_INSTR_RET"
					// "coreduo::INSTR_RET"
					// "coreduo::UNHALTED_REFERENCE_CYCLES"
					));
		}
		space.add(MeasurementDescription.measurerAxis,
				new TscMeasurerDescription());

		space.add(operationAxis, "ArithmeticOperation_ADD");
		// space.add(operationAxis, "ArithmeticOperation_MUL");
		// space.add(operationAxis, "ArithmeticOperation_MULADD");

		space.add(optimizationAxis, "-O3");
		// space.add(optimizationAxis, "-O3");

		for (int i = 1; i < 5; i++) {
			space.add(unrollAxis, i);
		}
		for (int i = 1; i < 5; i++) {
			space.add(dlpAxis, i);
		}

		double minCyclef = Double.POSITIVE_INFINITY;
		int minUnroll = 0;
		int minDlp = 0;

		PrintStream out = new PrintStream(outputName + ".data");
		for (Coordinate coordinate : space.getAllPoints(space
				.getAllAxesWithLeastSignificantAxes(optimizationAxis,
						measurerAxis, operationAxis, dlpAxis, unrollAxis,
						iterationsAxis

				))) {
			MeasurementDescription measurement = new MeasurementDescription(
					coordinate);

			MeasurementResult result = measurementAppController.measure(
					measurement, 10);

			DescriptiveStatistics statistics = null;
			if (coordinate.get(measurerAxis) instanceof PerfEventMeasurerDescription) {

				statistics = PerfEventMeasurerOutput.getStatistics("cycles",
						result);
			} else {
				statistics = TscMeasurerOutput.getStatistics(result);
			}
			// = ExecutionTimeMeasurerOutput.getStatistics(result);

			double cyclef = statistics.getMin()
					/ (coordinate.get(iterationsAxis)
							* coordinate.get(unrollAxis) * coordinate
								.get(dlpAxis));
			if (cyclef < minCyclef) {
				minCyclef = cyclef;
				minUnroll = coordinate.get(unrollAxis);
				minDlp = coordinate.get(dlpAxis);
			}
			System.out.printf("%s: %g %g %g\n", coordinate.toString(
					operationAxis, measurerAxis, iterationsAxis, unrollAxis,
					dlpAxis), statistics.getMin(), statistics.getPercentile(50)
					/ statistics.getMin(), cyclef);
			out.printf("%d %d %g\n", coordinate.get(dlpAxis),
					coordinate.get(unrollAxis), cyclef);
		}
		out.close();

		System.out.printf("Fastes Cycles/Flop: %g, unroll: %d dlp: %d\n",
				minCyclef, minUnroll, minDlp);
	}
}
