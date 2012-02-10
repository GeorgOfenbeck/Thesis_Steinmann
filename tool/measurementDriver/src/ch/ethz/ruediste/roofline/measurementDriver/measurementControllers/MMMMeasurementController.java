package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.MMMKernel.Algorithm;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.Operation;

import com.google.inject.Inject;

public class MMMMeasurementController implements IMeasurementController {

	public String getName() {
		return "mmm";
	}

	public String getDescription() {
		return "run the Matrix Matrix Multiplication kernel";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	public void measure(String outputName) throws IOException {
		ParameterSpace space = new ParameterSpace();
		for (long i = 16; i <= 16; i *= 2) {
			space.add(Axes.matrixSizeAxis, i);
		}
		Axis<Algorithm> algorithmAxis = new Axis<MMMKernel.Algorithm>(
				"4762fd9f-88d4-4bdd-b8fa-ae73c7996151", "algorithm");

		space.add(algorithmAxis, Algorithm.TripleLoop);
		space.add(algorithmAxis, Algorithm.Blocked);
		space.add(algorithmAxis, Algorithm.Blas);

		space.add(Axes.blockSizeAxis, 16L);

		space.add(Axes.optimizationAxis, "-O3");

		for (Coordinate coordinate : space) {
			MMMKernel kernel = new MMMKernel();
			kernel.setAlgorithm(coordinate.get(algorithmAxis));
			kernel.setMu(2);
			kernel.setNu(2);
			kernel.setKu(2);
			kernel.setNoCheck(false);
			kernel.initialize(coordinate);

			Performance performance = quantityMeasuringService
					.measurePerformance(kernel, Operation.CompInstr,
							ClockType.CoreCycles);
			System.out.printf("Performance %s: %s\n", coordinate, performance);

			OperationCount operationCount = quantityMeasuringService
					.measureOperationCount(kernel, Operation.CompInstr);
			System.out
					.printf("Operations %s: %s\n", coordinate, operationCount);

			TransferredBytes bytes = quantityMeasuringService
					.measureTransferredBytes(kernel,
							MemoryTransferBorder.LlcRam);

			System.out.printf("Transferred Bytes %s: %s\n", coordinate, bytes);
		}
	}
}
