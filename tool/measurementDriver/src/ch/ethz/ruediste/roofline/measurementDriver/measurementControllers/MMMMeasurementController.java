package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;
import java.util.HashMap;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.MMMKernel.MMMAlgorithm;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
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
		for (long i = 256; i <= 256; i *= 2) {
			space.add(Axes.matrixSizeAxis, i);
		}
		Axis<MMMAlgorithm> algorithmAxis = new Axis<MMMKernel.MMMAlgorithm>(
				"4762fd9f-88d4-4bdd-b8fa-ae73c7996151", "algorithm");

		//space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_TripleLoop);
		//space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_Blocked);
		space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_Blas_Openblas);
		space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_Blas_Mkl);

		HashMap<MMMAlgorithm, Operation> algorithmOperation = new HashMap<MMMKernel.MMMAlgorithm, QuantityMeasuringService.Operation>();
		algorithmOperation.put(MMMAlgorithm.MMMAlgorithm_TripleLoop,
				Operation.CompInstr);
		algorithmOperation.put(MMMAlgorithm.MMMAlgorithm_Blocked,
				Operation.CompInstr);
		algorithmOperation.put(MMMAlgorithm.MMMAlgorithm_Blas_Openblas,
				Operation.CompInstr);
		algorithmOperation.put(MMMAlgorithm.MMMAlgorithm_Blas_Mkl,
				Operation.DoublePrecisionFlop);

		space.add(Axes.blockSizeAxis, 16L);

		space.add(Axes.optimizationAxis, "-O3");

		for (Coordinate coordinate : space) {
			MMMKernel kernel = new MMMKernel();
			kernel.setAlgorithm(coordinate.get(algorithmAxis));
			kernel.setMu(2);
			kernel.setNu(2);
			kernel.setKu(2);
			kernel.setNoCheck(true);
			kernel.initialize(coordinate);

			Operation operation = algorithmOperation.get(coordinate
					.get(algorithmAxis));

			/*Performance performance = quantityMeasuringService
					.measurePerformance(kernel, operation, ClockType.CoreCycles);
			System.out.printf("Performance %s: %s\n", coordinate, performance);*/

			OperationCount operationCount = quantityMeasuringService
					.measureOperationCount(kernel, operation);
			System.out
					.printf("Operations %s: %s\n", coordinate, operationCount);

			/*TransferredBytes bytes = quantityMeasuringService
					.measureTransferredBytes(kernel,
							MemoryTransferBorder.LlcRam);*/

			// System.out.printf("Transferred Bytes %s: %s\n", coordinate, bytes);
		}
	}
}
