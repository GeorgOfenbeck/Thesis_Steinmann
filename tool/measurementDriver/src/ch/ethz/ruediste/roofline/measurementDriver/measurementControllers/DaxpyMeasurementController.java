package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.*;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController.Algorithm;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.Operation;

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
		rooflineController.addPeakPerformance("ADD", Algorithm.Add,
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("MUL", Algorithm.Mul,
				InstructionSet.SSE);
		rooflineController.addPeakThroughput("MemLoad", Algorithm.Load,
				MemoryTransferBorder.LlcRam);

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
