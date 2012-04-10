package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;

import com.google.inject.Inject;

public class DgemvMeasurementController implements IMeasurementController {

	public String getName() {
		return "dgemv";
	}

	public String getDescription() {
		return "run Matrix-Vector multiplication";
	}

	@Inject
	public SystemInfoService systemInfoService;

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	@Inject
	RooflineController rooflineController;

	@Inject
	Configuration configuration;

	public void measure(String outputName) throws IOException {
		rooflineController.setTitle("Matrix-Vector Multiplication");
		rooflineController.setOutputName(outputName);
		rooflineController.addDefaultPeaks();

		ParameterSpace space = new ParameterSpace();
		space.add(DaxpyKernel.useMklAxis, true);
		space.add(DaxpyKernel.useMklAxis, false);
		space.add(DaxpyKernel.numThreadsAxis, 1);
		space.add(DaxpyKernel.numThreadsAxis, systemInfoService.getOnlineCPUs()
				.size());

		for (Coordinate coord : space) {
			addRooflinePoints(rooflineController, coord);
		}
		rooflineController.plot();
	}

	/**
	 * @param rooflineController
	 * @param coord
	 */
	public void addRooflinePoints(RooflineController rooflineController,
			Coordinate coord) {
		configuration.push();
		for (long matrixSize = 500; matrixSize <= 6000; matrixSize += 500) {
			if (matrixSize > 2000) {
				configuration.set(QuantityMeasuringService.numberOfRunsKey, 1);
			}
			else {
				configuration.set(QuantityMeasuringService.numberOfRunsKey, 10);
			}
			DgemvKernel kernel = new DgemvKernel();
			kernel.initialize(coord);
			kernel.setOptimization("-O3");
			kernel.setMatrixSize(matrixSize);

			rooflineController
					.addRooflinePoint(kernel.getLabel(),
							Long.toString(matrixSize), kernel,
							kernel.getSuggestedOperation(),
							MemoryTransferBorder.LlcRamLines);
			/*rooflineController
					.addRooflinePoint(kernel.getLabel(),
							Long.toString(matrixSize), kernel,
							new OperationCount(2 * Math.pow(matrixSize, 2) + 3
									* matrixSize),
							MemoryTransferBorder.LlcRamLines);*/
		}
		configuration.pop();
	}
}
