package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.Axes.*;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.ArithmeticKernelDescription.ArithmeticOperation;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.Operation;

import com.google.inject.Inject;

public class ArithmeticMeasurementController implements IMeasurementController {
	private static Logger log = Logger
			.getLogger(ArithmeticMeasurementController.class);

	public String getName() {
		return "arithmetic";
	}

	public String getDescription() {
		return "performs all arithmetic measurements";
	}

	@Inject
	MeasurementAppController measurementAppController;

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	public void measure(String outputName) throws IOException {

		log.trace("entering arithmetic measurement controller");

		ParameterSpace space = new ParameterSpace();
		space.add(iterationsAxis, 10000L);
		// space.add(iterationsAxis, 100000L);

		space.add(arithmeticOperationAxis,
				ArithmeticOperation.ArithmeticOperation_ADD);
		// space.add(operationAxis, ArithmeticOperation.ArithmeticOperation_MUL);
		// space.add(operationAxis, ArithmeticOperation.ArithmeticOperation_MULADD);

		HashMap<InstructionSet, String> optimizationMap = new HashMap<InstructionSet, String>();
		HashMap<InstructionSet, Operation> operationMap = new HashMap<InstructionSet, Operation>();

		space.add(instructionSetAxis, InstructionSet.SSEScalar);
		optimizationMap
		.put(InstructionSet.SSEScalar, "-O3 -mfpmath=sse -msse2");
		operationMap.put(InstructionSet.SSEScalar,
				Operation.DoublePrecisionFlop);

		space.add(instructionSetAxis, InstructionSet.SSE);
		optimizationMap.put(InstructionSet.SSE, "-O3 -msse2");
		operationMap.put(InstructionSet.SSE, Operation.DoublePrecisionFlop);

		space.add(instructionSetAxis, InstructionSet.x87);
		optimizationMap.put(InstructionSet.x87, "-O3");
		operationMap.put(InstructionSet.x87, Operation.CompInstr);

		space.add(unrollAxis, 14);
		space.add(dlpAxis, 4);

		log.debug("starting space exploration");
		for (Coordinate coordinate : space.getAllPoints(space
				.getAllAxesWithLeastSignificantAxes(arithmeticOperationAxis,
						dlpAxis, unrollAxis,
						iterationsAxis

						))) {
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.initialize(coordinate);
			InstructionSet instructionSet = coordinate.get(instructionSetAxis);
			kernel.setOptimization(optimizationMap.get(instructionSet));

			if (true) {
				Performance performance = quantityMeasuringService
						.measurePerformance(kernel,
								operationMap.get(instructionSet),
								ClockType.CoreCycles);
				System.out.printf("Performance %s: %s\n", coordinate, performance);
			}

			OperationCount count = quantityMeasuringService
					.measureOperationCount(kernel,
							operationMap.get(instructionSet));
			System.out.printf("Operations %s: %s\n", coordinate, count);
		}

	}
}
