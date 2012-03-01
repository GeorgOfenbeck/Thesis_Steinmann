package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.infrastructure.services.*;
import ch.ethz.ruediste.roofline.sharedEntities.Operation;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.DaxpyKernel;

import com.google.inject.Inject;

public class DaxpyMeasurementController implements IMeasurementController {

	public String getName() {
		return "daxpy";
	}

	public String getDescription() {
		return "run Vector-Vector multiplication";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	@Inject
	RooflineController rooflineController;

	public void measure(String outputName) throws IOException {
		rooflineController.setTitle("Vector-Vector Multiplication");
		rooflineController.addDefaultPeaks();

		addPoints(rooflineController, true);
		addPoints(rooflineController, false);
		rooflineController.plot();
	}

	/**
	 * @param rooflineController
	 * @param useMkl
	 */
	public void addPoints(RooflineController rooflineController, boolean useMkl) {
		for (long vectorSize = 400; vectorSize < 5000; vectorSize += 100) {
			DaxpyKernel kernel = new DaxpyKernel();
			kernel.setOptimization("-O3");
			kernel.setVectorSize(vectorSize);
			kernel.setUseMkl(useMkl);

			rooflineController.addRooflinePoint(useMkl ? "VVM-Mkl"
					: "VVM-OpenBlas", Long.toString(vectorSize), kernel,
					Operation.DoublePrecisionFlop, MemoryTransferBorder.LlcRam);
		}
	}

}
