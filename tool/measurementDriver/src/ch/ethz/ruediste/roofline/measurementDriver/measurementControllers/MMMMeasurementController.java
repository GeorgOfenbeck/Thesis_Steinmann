package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.MMMKernel.MMMAlgorithm;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.*;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController.Algorithm;
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

	public void measure(String outputName) throws IOException {
		rooflineController.addPeakPerformance("ADD", Algorithm.Add,
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("MUL", Algorithm.Mul,
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("ABal", Algorithm.ArithBalanced,
				InstructionSet.SSE);

		rooflineController.addPeakThroughput("MemLoad", Algorithm.Load,
				MemoryTransferBorder.LlcRam);

		{
			ParameterSpace space = new ParameterSpace();
			for (long i = 64; i <= 400; i += 64) {
				space.add(Axes.matrixSizeAxis, i);
			}
			space.add(Axes.blockSizeAxis, 64L);

			space.add(Axes.optimizationAxis, "-O3");
			Axis<ch.ethz.ruediste.roofline.dom.MMMKernel.MMMAlgorithm> algorithmAxis = new Axis<MMMKernel.MMMAlgorithm>(
					"742250a7-5ea2-4a39-b0c6-7145d0c4b292", "algorithm");

			space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_TripleLoop);
			space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_Blocked);
			space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_Blas_Openblas);
			space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_Blas_Mkl);

			for (Coordinate coordinate : space.getAllPoints(space
					.getAllAxesWithMostSignificantAxes(algorithmAxis))) {
				switch (coordinate.get(algorithmAxis)) {
				case MMMAlgorithm_TripleLoop:
				case MMMAlgorithm_Blocked:
					if (coordinate.get(Axes.matrixSizeAxis) > 704) {
						continue;
					}
				break;
				}
				MMMKernel kernel = new MMMKernel();
				kernel.setMu(2);
				kernel.setNu(2);
				kernel.setKu(2);
				kernel.setNoCheck(true);
				kernel.setAlgorithm(coordinate.get(algorithmAxis));
				kernel.initialize(coordinate);

				String seriesName = StringUtils.removeStart(
						coordinate.get(algorithmAxis).toString(),
						"MMMAlgorithm_");

				String label = coordinate.get(Axes.matrixSizeAxis).toString();
				System.out.printf("Measuring %s\n", label);

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
		}

		rooflineController.plot();
	}
}
