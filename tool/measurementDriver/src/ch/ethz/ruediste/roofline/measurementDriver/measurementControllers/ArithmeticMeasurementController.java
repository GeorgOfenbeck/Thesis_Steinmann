package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.MeasurementDescription.*;

import java.io.IOException;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
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
		ParameterSpace space = new ParameterSpace();
		space.add(iterationsAxis, 10000L);
		// space.add(iterationsAxis, 100000L);

		space.add(kernelAxis, new ArithmeticKernelDescription());
		// space.add(kernelAxis, new ArithmeticSingleKernelDescription());
		space.add(measurementSchemeAxis,
				new SimpleMeasurementSchemeDescription());

		{
			space.add(MeasurementDescription.measurerAxis,
					new PerfEventMeasurerDescription(
							"cycles", "coreduo::UNHALTED_CORE_CYCLES"
					// "coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:PACKED_DOUBLE"
					// "coreduo::FP_COMP_INSTR_RET"
					// "coreduo::INSTR_RET"
					// "coreduo::UNHALTED_REFERENCE_CYCLES"
					));
		}

		space.add(operationAxis, "ArithmeticOperation_ADD");
		// space.add(operationAxis, "ArithmeticOperation_MUL");
		// space.add(operationAxis, "ArithmeticOperation_MULADD");

		space.add(optimizationAxis, "-O3");
		// space.add(optimizationAxis, "-O3");

		space.add(unrollAxis, 1);
		space.add(unrollAxis, 2);
		space.add(unrollAxis, 4);
		space.add(unrollAxis, 8);
		space.add(unrollAxis, 16);
		// space.add(unrollAxis, 32);
		// space.add(unrollAxis, 64);

		space.add(dlpAxis, 1);
		space.add(dlpAxis, 2);
		space.add(dlpAxis, 3);
		space.add(dlpAxis, 4);
		space.add(dlpAxis, 5);
		space.add(dlpAxis, 6);
		space.add(dlpAxis, 7);
		space.add(dlpAxis, 8);
		space.add(dlpAxis, 16);
		// space.add(dlpAxis, 32);

		for (Coordinate coordinate : space.getAllPoints(space
				.getAllAxesWithLeastSignificantAxes(optimizationAxis,
						operationAxis,
						dlpAxis,
						unrollAxis, iterationsAxis

				))) {
			MeasurementDescription measurement = new MeasurementDescription(
					coordinate);

			MeasurementResult result = measurementRepository
					.getMeasurementResults(
							measurement, 10);
			DescriptiveStatistics statistics = PerfEventMeasurerOutput
					.getStatistics("cycles", result);
			// = ExecutionTimeMeasurerOutput.getStatistics(result);

			System.out.printf(
					"%s: %g %g %g\n",
					coordinate.toString(operationAxis, iterationsAxis,
							unrollAxis, dlpAxis),
					statistics.getMin(),
					statistics.getPercentile(50) / statistics.getMin(),
					statistics.getMin()
							/ (coordinate.get(iterationsAxis)
									* coordinate.get(unrollAxis) * coordinate
										.get(dlpAxis))
					);
		}
	}
}
