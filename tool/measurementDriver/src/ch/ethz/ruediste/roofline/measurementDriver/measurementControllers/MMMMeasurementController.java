package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.Axes.matrixSizeAxis;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.MMMKernel.MMMAlgorithm;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
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

	@Inject
	RooflineController rooflineController;

	@Inject
	Configuration configuration;

	public void measure(String outputName) throws IOException {
		rooflineController.setTitle("Matrix-Matrix-Multiplication");
		rooflineController.addDefaultPeaks();

		addSeries(rooflineController, MMMAlgorithm.MMMAlgorithm_TripleLoop,
				MMMAlgorithm.MMMAlgorithm_Blas_Openblas,
				MMMAlgorithm.MMMAlgorithm_Blas_Mkl);

		addBlockedSeries(rooflineController);

		rooflineController.plot();
	}

	/**
	 * @param rooflineController
	 */
	public void addBlockedSeries(RooflineController rooflineController) {
		{
			configuration.push();
			for (long i = 64; i <= 704; i += 64) {
				if (i < 400) {
					configuration.set(
							QuantityMeasuringService.numberOfMeasurementsKey,
							10);
				}
				else {
					configuration
					.set(QuantityMeasuringService.numberOfMeasurementsKey,
							1);
				}

				MMMKernel kernel = new MMMKernel();
				kernel.setMatrixSize(i);
				kernel.setMu(2);
				kernel.setNu(2);
				kernel.setKu(2);
				kernel.setNb(16);
				kernel.setNoCheck(true);
				kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blocked);
				kernel.setOptimization("-O3");

				String label = Long.toString(i);
				System.out.printf("Measuring %s\n", label);

				Operation operation = Operation.CompInstr;

				rooflineController.addRooflinePoint("MMM-Blocked", label,
						kernel, operation, MemoryTransferBorder.LlcRam);

				/*Performance performance = quantityMeasuringService
				.measurePerformance(kernel, operation, ClockType.CoreCycles);
				System.out.printf("Performance %s: %s\n", coordinate, performance);*/

				OperationCount operationCount = quantityMeasuringService
						.measureOperationCount(kernel, operation);
				System.out.printf("Operations Blocked %d: %s\n", i,
						operationCount);

				/*TransferredBytes bytes = quantityMeasuringService
						.measureTransferredBytes(kernel,
								MemoryTransferBorder.LlcRam);*/

				// System.out.printf("Transferred Bytes %s: %s\n", coordinate, bytes);
			}
			configuration.pop();
		}
	}

	/**
	 * series may not be blocked
	 */
	public void addSeries(RooflineController rooflineController,
			MMMAlgorithm... algorithms) {
		{
			configuration.push();
			ParameterSpace space = new ParameterSpace();
			for (long i = 100; i <= 2000; i += 100) {
				space.add(Axes.matrixSizeAxis, i);
			}

			space.add(Axes.optimizationAxis, "-O3");
			Axis<ch.ethz.ruediste.roofline.dom.MMMKernel.MMMAlgorithm> algorithmAxis = new Axis<MMMKernel.MMMAlgorithm>(
					"742250a7-5ea2-4a39-b0c6-7145d0c4b292", "algorithm");

			for (MMMAlgorithm algorithm : algorithms) {
				if (algorithm == MMMAlgorithm.MMMAlgorithm_Blocked) {
					throw new Error(
							"use addBlockedSeries for Blocked algorithm");
				}
				space.add(algorithmAxis, algorithm);
			}

			for (Coordinate coordinate : space.getAllPoints(space
					.getAllAxesWithMostSignificantAxes(algorithmAxis))) {
				if (coordinate.get(matrixSizeAxis) < 400) {
					configuration.set(
							QuantityMeasuringService.numberOfMeasurementsKey,
							10);
				}
				else {
					configuration
					.set(QuantityMeasuringService.numberOfMeasurementsKey,
							1);
				}
				// skip large sizes for tripple loop
				if (coordinate.get(algorithmAxis) == MMMAlgorithm.MMMAlgorithm_TripleLoop
						&& coordinate.get(Axes.matrixSizeAxis) > 704) {
					continue;
				}
				MMMKernel kernel = new MMMKernel();
				kernel.setNoCheck(true);
				kernel.setAlgorithm(coordinate.get(algorithmAxis));
				kernel.initialize(coordinate);

				// get the name of the series
				String seriesName;
				switch (coordinate.get(algorithmAxis)) {
				case MMMAlgorithm_Blas_Mkl:
					seriesName = "MMM-Mkl";
					break;
				case MMMAlgorithm_Blas_Openblas:
					seriesName = "MMM-OpenBlas";
					break;
				case MMMAlgorithm_TripleLoop:
					seriesName = "MMM-TripleLoop";
					break;
				default:
					throw new Error("Should not happen");

				}

				// get the label for the point
				String label = coordinate.get(Axes.matrixSizeAxis).toString();

				// get the operation to be measured
				Operation operation = Operation.CompInstr;
				switch (coordinate.get(algorithmAxis)) {
				case MMMAlgorithm_Blas_Mkl:
					operation = Operation.DoublePrecisionFlop;
					break;
				case MMMAlgorithm_Blas_Openblas:
					operation = Operation.CompInstr;
					break;
				}

				rooflineController.addRooflinePoint(seriesName, label, kernel,
						operation, MemoryTransferBorder.LlcRam);

				/*Performance performance = quantityMeasuringService
				.measurePerformance(kernel, operation, ClockType.CoreCycles);
				System.out.printf("Performance %s: %s\n", coordinate, performance);*/

				OperationCount operationCount = quantityMeasuringService
						.measureOperationCount(kernel, operation);
				System.out.printf("Operations %s: %s\n", coordinate,
						operationCount);

				/*TransferredBytes bytes = quantityMeasuringService
						.measureTransferredBytes(kernel,
								MemoryTransferBorder.LlcRam);*/

				// System.out.printf("Transferred Bytes %s: %s\n", coordinate, bytes);
			}
			configuration.pop();
		}
	}
}
