package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.KeyPosition;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
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
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public RooflineController rooflineController;

	@Inject
	public Configuration configuration;

	@Inject
	public SystemInfoService systemInfoService;

	private boolean IsPowerOfTwo(long x) {
		return (x & (x - 1)) == 0;
	}

	@SuppressWarnings("unchecked")
	public void measure(String outputName) throws IOException {
		rooflineController.setTitle("Fast Fourier Transformation");
		rooflineController.setOutputName(outputName);
		rooflineController.addDefaultPeaks();
		rooflineController
				.getPlot()
				.setAutoscaleY(true)
				.setKeyPosition(KeyPosition.BottomRight);

		addPoints(rooflineController, 1, FFTnrKernel.class, FFTmklKernel.class,
				FFTfftwKernel.class, FFTSpiralKernel.class);

		addPoints(rooflineController, systemInfoService.getOnlineCPUs().size(),
				FFTmklKernel.class, FFTfftwKernel.class);

		rooflineController.plot();
	}

	/**
	 * @param rooflineController
	 */
	public void addPoints(RooflineController rooflineController,
			int numThreads,
			Class<? extends FFTKernelBase>... clazzes) {
		ParameterSpace space = new ParameterSpace();
		for (long i = 32; i <= 10000 * 1024L; i *= 2) {
			space.add(Axes.bufferSizeAxis, i);
		}

		//space.add(Axes.bufferSizeAxis, 64 * 1024L);

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

		space.add(Axes.optimizationAxis, "-O3 -msse2");

		configuration.push();
		for (Coordinate coordinate : space.getAllPoints(kernelAxis, null)) {

			if (coordinate.get(bufferSizeAxis) > 128 * 1024L) {
				configuration.set(QuantityMeasuringService.numberOfRunsKey, 1);
			}
			else {
				configuration
						.set(QuantityMeasuringService.numberOfRunsKey, 100);
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
					&& coordinate.get(bufferSizeAxis) > 1024 * 1024L) {
				continue;
			}

			// skip large buffer sizes for the Spiral kernel (does not support them)
			if (coordinate.get(kernelAxis) instanceof FFTSpiralKernel
					&& coordinate.get(bufferSizeAxis) > 8192L) {
				continue;
			}

			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);
			kernel.setNumThreads(numThreads);

			rooflineController.addRooflinePoint(
					kernel.getLabel(),
					coordinate.get(bufferSizeAxis), kernel,
					kernel.getSuggestedOperation(),
					MemoryTransferBorder.LlcRamLines);
		}
		configuration.pop();
	}

}
