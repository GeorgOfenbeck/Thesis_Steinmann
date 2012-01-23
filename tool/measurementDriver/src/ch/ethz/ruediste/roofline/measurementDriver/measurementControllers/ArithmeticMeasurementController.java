package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.Axes.*;

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.ArithmeticKernelDescription;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Performance;
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

		log.debug("entering arithmetic measurement controller");

		ParameterSpace space = new ParameterSpace();
		space.add(iterationsAxis, 10000L);
		// space.add(iterationsAxis, 100000L);

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

		Performance maxPerformance = null;
		Coordinate maxCoordinate = null;

		log.debug("starting space exploration");
		for (Coordinate coordinate : space.getAllPoints(space
				.getAllAxesWithLeastSignificantAxes(optimizationAxis,
						operationAxis, dlpAxis, unrollAxis,
						iterationsAxis

				))) {
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.initialize(coordinate);
			Performance performance = quantityMeasuringService
					.measurePerformance(kernel, Operation.x87,
							ClockType.CoreCycles);
			System.out.printf("%s: %s\n", coordinate, performance);

			if (maxPerformance == null
					|| performance.getValue() > maxPerformance.getValue()) {
				maxPerformance = performance;
				maxCoordinate = coordinate;
			}
		}

		System.out.printf("Fastest: %s: %s\n",
				maxCoordinate, maxPerformance);
	}
}
