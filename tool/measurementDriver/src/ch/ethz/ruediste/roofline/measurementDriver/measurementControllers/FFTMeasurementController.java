package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.Axes.bufferSizeAxis;

import java.io.IOException;
import java.util.HashMap;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.FFTKernel.FFTAlgorithm;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.*;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController.Algorithm;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.Operation;

import com.google.inject.Inject;

public class FFTMeasurementController implements IMeasurementController {

	public String getName() {
		return "fft";
	}

	public String getDescription() {
		return "runs an FFT computation";
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

		rooflineController.addPeakThroughput("MemLoad", Algorithm.Load,
				MemoryTransferBorder.LlcRam);

		ParameterSpace space = new ParameterSpace();
		for (long i = 1024; i <= 256 * 1024; i *= 2) {
			space.add(Axes.bufferSizeAxis, i);
		}

		Axis<FFTAlgorithm> algorithmAxis = new Axis<FFTAlgorithm>(
				"a0cb4c78-6a15-460b-b7fc-423c6663472a", "algorithm");

		space.add(algorithmAxis, FFTAlgorithm.FFTAlgorithm_NR);
		space.add(algorithmAxis, FFTAlgorithm.FFTAlgorithm_MKL);

		HashMap<FFTAlgorithm, String> algorithmName = new HashMap<FFTKernel.FFTAlgorithm, String>();
		algorithmName.put(FFTAlgorithm.FFTAlgorithm_MKL, "mkl");
		algorithmName.put(FFTAlgorithm.FFTAlgorithm_NR, "nr");

		HashMap<FFTAlgorithm, Operation> algorithmOperation = new HashMap<FFTKernel.FFTAlgorithm, QuantityMeasuringService.Operation>();
		algorithmOperation.put(FFTAlgorithm.FFTAlgorithm_NR,
				Operation.CompInstr);
		algorithmOperation.put(FFTAlgorithm.FFTAlgorithm_MKL,
				Operation.DoublePrecisionFlop);

		space.add(Axes.optimizationAxis, "-O3");

		for (Coordinate coordinate : space) {
			FFTKernel kernel = new FFTKernel();
			kernel.setAlgorithm(coordinate.get(algorithmAxis));
			kernel.setNoCheck(false);
			kernel.initialize(coordinate);

			Performance performance = quantityMeasuringService
					.measurePerformance(kernel, algorithmOperation
							.get(coordinate.get(algorithmAxis)),
							ClockType.CoreCycles);
			System.out.printf("Performance %s: %s\n", coordinate, performance);

			OperationCount operationCount = quantityMeasuringService
					.measureOperationCount(kernel, algorithmOperation
							.get(coordinate.get(algorithmAxis)));
			System.out
					.printf("Operations %s: %s\n", coordinate, operationCount);

			TransferredBytes bytes = quantityMeasuringService
					.measureTransferredBytes(kernel,
							MemoryTransferBorder.LlcRam);

			System.out.printf("Transferred Bytes %s: %s\n", coordinate, bytes);

			rooflineController.addRooflinePoint(
					algorithmName.get(coordinate.get(algorithmAxis))
							+ coordinate.get(bufferSizeAxis), kernel,
					operationCount, MemoryTransferBorder.LlcRam);
		}
		rooflineController.plot();
	}

}
