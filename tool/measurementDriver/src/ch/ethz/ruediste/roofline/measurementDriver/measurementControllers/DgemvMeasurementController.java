package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.services.*;
import ch.ethz.ruediste.roofline.sharedEntities.Operation;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.DgemvKernel;

import com.google.inject.Inject;

public class DgemvMeasurementController implements IMeasurementController {

	public String getName() {
		return "dgemv";
	}

	public String getDescription() {
		return "run Matrix-Vector multiplication";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	@Inject
	RooflineController rooflineController;

	public void measure(String outputName) throws IOException {
		rooflineController.setTitle("Matrix-Vector Multiplication");
		rooflineController.addDefaultPeaks();

		addRooflinePoints(rooflineController, true);
		addRooflinePoints(rooflineController, false);
		rooflineController.plot();
	}

	/**
	 * @param rooflineController
	 * @param useMkl
	 */
	public void addRooflinePoints(RooflineController rooflineController,
			boolean useMkl) {
		for (long matrixSize = 100; matrixSize < 2000; matrixSize += 100) {
			DgemvKernel kernel = new DgemvKernel();
			kernel.setOptimization("-O3");
			kernel.setMatrixSize(matrixSize);
			kernel.setUseMkl(useMkl);

			rooflineController.addRooflinePoint(useMkl ? "MVM-Mkl"
					: "MVM-OpenBlas", Long.toString(matrixSize), kernel,
					useMkl ? Operation.DoublePrecisionFlop
							: Operation.DoublePrecisionFlop,
					MemoryTransferBorder.LlcRam);
		}
	}

}
