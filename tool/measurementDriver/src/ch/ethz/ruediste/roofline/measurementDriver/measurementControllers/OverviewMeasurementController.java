package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.*;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController.Algorithm;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.InstructionSet;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

import com.google.inject.Inject;

public class OverviewMeasurementController implements IMeasurementController {

	public String getName() {
		return "overview";
	}

	public String getDescription() {
		return "plots an overview of different algorithms";
	}

	@Inject
	RooflineController rooflineController;

	@Inject
	DaxpyMeasurementController daxpyMeasurementController;

	@Inject
	DgemvMeasurementController dgemvMeasurementController;

	@Inject
	MMMMeasurementController mmmMeasurementController;

	@Inject
	FFTMeasurementController fftMeasurementController;

	@SuppressWarnings("unchecked")
	public void measure(String outputName) throws IOException {
		rooflineController.setTitle("Overview");
		rooflineController.addPeakPerformance("ADD", Algorithm.Add,
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("MUL", Algorithm.Mul,
				InstructionSet.SSE);
		rooflineController.addPeakThroughput("MemLoad", Algorithm.Load,
				MemoryTransferBorder.LlcRam);

		daxpyMeasurementController.addPoints(rooflineController, true);
		dgemvMeasurementController.addRooflinePoints(rooflineController, true);
		mmmMeasurementController.addSeries(rooflineController,
				MMMAlgorithm.MMMAlgorithm_Blas_Mkl);

		fftMeasurementController.addPoints(rooflineController,
				FFTmklKernel.class);
		rooflineController.plot();
	}
}
