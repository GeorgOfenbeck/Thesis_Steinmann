package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
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
		for (long i = 64; i <= 128; i *= 2) {
			space.add(Axes.matrixSizeAxis, i);
		}
		space.add(Axes.blockSizeAxis, 16L);

		space.add(Axes.optimizationAxis, "-O3");

		for (Coordinate coordinate : space) {
			MMMKernelDescription kernel = new MMMKernelDescription();
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
