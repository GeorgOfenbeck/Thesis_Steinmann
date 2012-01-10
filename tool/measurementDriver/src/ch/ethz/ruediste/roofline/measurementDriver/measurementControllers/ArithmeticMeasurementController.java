package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.MeasurementDescription.iterationsAxis;
import static ch.ethz.ruediste.roofline.dom.MeasurementDescription.kernelAxis;
import static ch.ethz.ruediste.roofline.dom.MeasurementDescription.measurementSchemeAxis;
import static ch.ethz.ruediste.roofline.dom.MeasurementDescription.operationAxis;
import static ch.ethz.ruediste.roofline.dom.MeasurementDescription.optimizationAxis;
import static ch.ethz.ruediste.roofline.dom.MeasurementDescription.unrollAxis;

import java.io.IOException;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ch.ethz.ruediste.roofline.dom.ArithmeticSingleKernelDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementDescription;
import ch.ethz.ruediste.roofline.dom.MeasurementResult;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerDescription;
import ch.ethz.ruediste.roofline.dom.PerfEventMeasurerOutput;
import ch.ethz.ruediste.roofline.dom.SimpleMeasurementSchemeDescription;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace;
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
		space.add(iterationsAxis, 100000L);

		// space.add(kernelAxis, new ArithmeticKernelDescription());
		space.add(kernelAxis, new ArithmeticSingleKernelDescription());
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
		space.add(operationAxis, "ArithmeticOperation_MUL");
		space.add(operationAxis, "ArithmeticOperation_MULADD");

		space.add(optimizationAxis, "-O3 -msse2");
		space.add(optimizationAxis, "-O3");

		space.add(unrollAxis, 1);
		space.add(unrollAxis, 2);
		space.add(unrollAxis, 4);
		space.add(unrollAxis, 8);
		space.add(unrollAxis, 16);
		space.add(unrollAxis, 32);
		space.add(unrollAxis, 64);

		for (Coordinate coordinate : space.getAllPoints(space
				.getAllAxesWithLeastSignificantAxes(optimizationAxis,
						unrollAxis, iterationsAxis
						, operationAxis
				))) {
			MeasurementDescription measurement = new MeasurementDescription(
					coordinate);

			MeasurementResult result = measurementRepository
					.getMeasurementResults(
							measurement, 10);
			DescriptiveStatistics statistics = PerfEventMeasurerOutput
					.getStatistics("cycles", result);
			// = ExecutionTimeMeasurerOutput.getStatistics(result);

			System.out.printf("%s: %g %g\n",
					coordinate.toString(operationAxis, iterationsAxis,
							unrollAxis, optimizationAxis),
					statistics.getMin(),
					statistics.getPercentile(50) / statistics.getMin());
		}
	}
}
