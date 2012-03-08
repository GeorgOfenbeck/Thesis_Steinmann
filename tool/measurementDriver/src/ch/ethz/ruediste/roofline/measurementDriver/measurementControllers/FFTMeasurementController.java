package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;
import java.util.HashMap;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.TransferredBytes;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;

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

	@Inject
	Configuration configuration;

	private boolean IsPowerOfTwo(long x) {
		return (x & (x - 1)) == 0;
	}

	@SuppressWarnings("unchecked")
	public void measure(String outputName) throws IOException {
		rooflineController.setTitle("Fast Fourier Transformation");
		rooflineController.setOutputName(outputName);
		rooflineController.addDefaultPeaks();

		addPoints(rooflineController, FFTnrKernel.class, FFTmklKernel.class,
				FFTfftwKernel.class, FFTSpiralKernel.class);
		rooflineController.plot();
	}

	/**
	 * @param rooflineController
	 */
	public void addPoints(RooflineController rooflineController,
			Class<? extends FFTKernelBase>... clazzes) {
		ParameterSpace space = new ParameterSpace();
		for (long i = 32; i <= 10000 * 1024L; i *= 2) {
			space.add(Axes.bufferSizeAxis, i);
		}

		/*for (long i = 72; i < 128; i += 8) {
			space.add(Axes.bufferSizeAxis, i * 1024L);
		}*/

		for (Class<? extends FFTKernelBase> clazz : clazzes) {
			try {
				space.add(kernelAxis, clazz.getConstructor().newInstance());
			}
			catch (Exception e) {
				throw new Error(e);
			}
		}

		HashMap<Class<?>, String> algorithmName = new HashMap<Class<?>, String>();
		algorithmName.put(FFTfftwKernel.class, "FFT-FFTW");
		algorithmName.put(FFTnrKernel.class, "FFT-NR");
		algorithmName.put(FFTmklKernel.class, "FFT-MKL");
		algorithmName.put(FFTSpiralKernel.class, "FFT-Spiral");

		HashMap<Class<?>, Operation> algorithmOperation = new HashMap<Class<?>, Operation>();
		algorithmOperation.put(FFTnrKernel.class, Operation.CompInstr);
		algorithmOperation.put(FFTmklKernel.class,
				Operation.DoublePrecisionFlop);
		algorithmOperation.put(FFTfftwKernel.class,
				Operation.DoublePrecisionFlop);
		algorithmOperation.put(FFTSpiralKernel.class,
				Operation.DoublePrecisionFlop);

		space.add(Axes.optimizationAxis, "-O3 -msse2");

		configuration.push();
		for (Coordinate coordinate : space) {

			if (coordinate.get(bufferSizeAxis) > 64 * 1024L) {
				configuration.set(QuantityMeasuringService.numberOfRunsKey, 1);
			}
			else {
				configuration.set(QuantityMeasuringService.numberOfRunsKey, 10);
			}
			// skip non-power of two sizes for the NR kernel
			if (coordinate.get(kernelAxis) instanceof FFTnrKernel
					&& !IsPowerOfTwo(coordinate.get(bufferSizeAxis))) {
				continue;
			}

			// skip large buffer sizes for the NR kernel
			if (coordinate.get(kernelAxis) instanceof FFTnrKernel
					&& coordinate.get(bufferSizeAxis) > 1024 * 1024L) {
				continue;
			}

			// skip large buffer sizes for the FFTW kernel
			if (coordinate.get(kernelAxis) instanceof FFTfftwKernel
					&& coordinate.get(bufferSizeAxis) > 128 * 1024L) {
				continue;
			}

			// skip large buffer sizes for the Spiral kernel (does not support them)
			if (coordinate.get(kernelAxis) instanceof FFTSpiralKernel
					&& coordinate.get(bufferSizeAxis) > 8192L) {
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

			rooflineController.addRooflinePoint(
					algorithmName.get(kernel.getClass()),
					coordinate.get(bufferSizeAxis).toString(), kernel,
					operation, MemoryTransferBorder.LlcRam);
		}
		configuration.pop();
	}
}
