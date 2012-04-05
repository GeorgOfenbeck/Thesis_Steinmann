package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.KeyPosition;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

import com.google.inject.Inject;

public class MMMMeasurementController implements IMeasurementController {

	public String getName() {
		return "mmm";
	}

	public String getDescription() {
		return "run the Matrix Matrix Multiplication kernel";
	}

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public RooflineController rooflineController;

	@Inject
	public Configuration configuration;

	@Inject
	public SystemInfoService systemInfoService;

	public void measure(String outputName) throws IOException {
		rooflineController.setTitle("Matrix-Matrix-Multiplication");
		rooflineController.setOutputName(outputName);
		rooflineController.addDefaultPeaks();
		rooflineController.getPlot().setKeyPosition(KeyPosition.BottomRight);

		{
			ParameterSpace space = new ParameterSpace();

			space.add(MMMKernel.MMMAlgorithmAxis,
					MMMAlgorithm.MMMAlgorithm_TrippleLoop);
			space.add(MMMKernel.MMMAlgorithmAxis,
					MMMAlgorithm.MMMAlgorithm_Blocked);
			space.add(MMMKernel.MMMAlgorithmAxis,
					MMMAlgorithm.MMMAlgorithm_Blocked_Restrict);

			space.add(BlasKernelBase.numThreadsAxis, 1);

			for (Coordinate coordinate : space) {
				addSeries(rooflineController, coordinate);
			}
		}

		{
			ParameterSpace space = new ParameterSpace();

			space.add(MMMKernel.MMMAlgorithmAxis,
					MMMAlgorithm.MMMAlgorithm_Blas);

			space.add(BlasKernelBase.numThreadsAxis, 1);
			space.add(BlasKernelBase.numThreadsAxis, systemInfoService
					.getOnlineCPUs().size());

			space.add(BlasKernelBase.useMklAxis, false);
			space.add(BlasKernelBase.useMklAxis, true);

			for (Coordinate coordinate : space) {
				addSeries(rooflineController, coordinate);
			}
		}

		rooflineController.plot();
	}

	/**
	 * series may not be blocked
	 * 
	 * @param multiThreaded
	 *            TODO
	 */
	public void addSeries(RooflineController rooflineController,
			Coordinate coordinate) {
		{
			// save configuration
			configuration.push();

			for (long matrixSize = 100; matrixSize <= 2000; matrixSize += 100) {

				// set number of runs dependant on matrix size
				if (matrixSize < 400) {
					configuration.set(QuantityMeasuringService.numberOfRunsKey,
							10);
				}
				else {
					configuration.set(QuantityMeasuringService.numberOfRunsKey,
							1);
				}

				// skip large sizes for tripple loop
				if (coordinate.get(MMMKernel.MMMAlgorithmAxis) == MMMAlgorithm.MMMAlgorithm_TrippleLoop
						&& matrixSize > 704) {
					continue;
				}

				// skip large sizes for blocked
				if (coordinate.get(MMMKernel.MMMAlgorithmAxis) == MMMAlgorithm.MMMAlgorithm_Blocked
						&& matrixSize > 1200) {
					continue;
				}

				// skip large sizes for blocked restrict
				if (coordinate.get(MMMKernel.MMMAlgorithmAxis) == MMMAlgorithm.MMMAlgorithm_Blocked_Restrict
						&& matrixSize > 1200) {
					continue;
				}

				MMMKernel kernel = new MMMKernel();
				kernel.initialize(coordinate);
				kernel.setMu(2);
				kernel.setNu(2);
				kernel.setKu(2);
				kernel.setNb(50);
				kernel.setOptimization("-O3");
				kernel.setNoCheck(true);
				kernel.setMatrixSize(matrixSize);

				rooflineController.addRooflinePoint(kernel.getLabel(),
						Long.toString(matrixSize), kernel,
						kernel.getSuggestedOperation(),
						MemoryTransferBorder.LlcRamLines);

				/*Performance performance = quantityMeasuringService
				.measurePerformance(kernel, operation, ClockType.CoreCycles);
				System.out.printf("Performance %s: %s\n", coordinate, performance);*/

				/*OperationCount operationCount = quantityMeasuringService
						.measureOperationCount(kernel, operation);
				System.out.printf("Operations %s: %s\n", coordinate,
						operationCount);*/

				/*TransferredBytes bytes = quantityMeasuringService
						.measureTransferredBytes(kernel,
								MemoryTransferBorder.LlcRamBus);*/

				// System.out.printf("Transferred Bytes %s: %s\n", coordinate, bytes);
			}
			configuration.pop();
		}
	}
}
