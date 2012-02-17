package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.Axes.*;

import java.io.IOException;
import java.util.HashMap;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.TransferredBytes;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
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

	private boolean IsPowerOfTwo(long x) {
		return (x & (x - 1)) == 0;
	}

	public void measure(String outputName) throws IOException {
		/*rooflineController.addPeakPerformance("ADD", Algorithm.Add,
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("MUL", Algorithm.Mul,
				InstructionSet.SSE);

		rooflineController.addPeakThroughput("MemLoad", Algorithm.Load,
				MemoryTransferBorder.LlcRam);*/

		ParameterSpace space = new ParameterSpace();
		for (long i = 1; i <= 512; i *= 2) {
			space.add(Axes.bufferSizeAxis, i * 1024);
		}

		/*for (long i = 72; i < 128; i += 8) {
			space.add(Axes.bufferSizeAxis, i * 1024L);
		}*/

		space.add(kernelAxis, new FFTnrKernel());
		//space.add(kernelAxis, new FFTmklKernel());
		//space.add(kernelAxis, new FFTfftwKernel());

		HashMap<Class<?>, String> algorithmName = new HashMap<Class<?>, String>();
		algorithmName.put(FFTfftwKernel.class, "fftw");
		algorithmName.put(FFTnrKernel.class, "nr");
		algorithmName.put(FFTmklKernel.class, "mkl");

		HashMap<Class<?>, Operation> algorithmOperation = new HashMap<Class<?>, QuantityMeasuringService.Operation>();
		algorithmOperation.put(FFTnrKernel.class, Operation.CompInstr);
		algorithmOperation.put(FFTmklKernel.class,
				Operation.DoublePrecisionFlop);
		algorithmOperation.put(FFTfftwKernel.class,
				Operation.DoublePrecisionFlop);

		space.add(Axes.optimizationAxis, "-O3");

		for (Coordinate coordinate : space) {

			// skip non-power of two sizes for the NR kernel
			if (coordinate.get(kernelAxis) instanceof FFTnrKernel
					&& !IsPowerOfTwo(coordinate.get(bufferSizeAxis))) {
				continue;
			}

			// skip large buffer sizes for the NR kernel
			if (coordinate.get(kernelAxis) instanceof FFTnrKernel
					&& coordinate.get(bufferSizeAxis) > 256 * 1024L) {
				continue;
			}

			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			Operation operation = algorithmOperation.get(kernel.getClass());
			/*
						Performance performance = quantityMeasuringService
								.measurePerformance(kernel, operation, ClockType.CoreCycles);
						System.out.printf("Performance %s: %s\n", coordinate, performance);

						OperationCount operationCount = quantityMeasuringService
								.measureOperationCount(kernel, operation);
						System.out
						.printf("Operations %s: %s\n", coordinate, operationCount);*/

			TransferredBytes bytes = quantityMeasuringService
					.measureTransferredBytes(kernel,
							MemoryTransferBorder.LlcRam);

			System.out.printf("Transferred Bytes %s: %s\n", coordinate, bytes);

			/*rooflineController.addRooflinePoint(
					algorithmName.get(kernel.getClass())
					+ coordinate.get(bufferSizeAxis), kernel,
					operationCount, MemoryTransferBorder.LlcRam);*/
		}
		//rooflineController.plot();
	}
}
