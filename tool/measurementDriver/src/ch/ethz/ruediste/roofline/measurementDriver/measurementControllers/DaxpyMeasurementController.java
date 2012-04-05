package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.KeyPosition;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
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
	public SystemInfoService systemInfoService;

	@Inject
	RooflineController rooflineController;

	public void measure(String outputName) throws IOException {
		rooflineController.setTitle("Vector-Vector Multiplication");
		rooflineController.setOutputName(outputName);
		rooflineController.addDefaultPeaks();
		rooflineController.getPlot().setAutoscaleY(true)
				.setKeyPosition(KeyPosition.BottomRight);

		ParameterSpace space = new ParameterSpace();
		space.add(DaxpyKernel.useMklAxis, true);
		space.add(DaxpyKernel.useMklAxis, false);
		space.add(DaxpyKernel.numThreadsAxis, 1);
		space.add(DaxpyKernel.numThreadsAxis, systemInfoService.getOnlineCPUs()
				.size());

		for (Coordinate coord : space) {
			addPoints(rooflineController, coord);
		}
		rooflineController.plot();
	}

	public void addPoints(RooflineController rooflineController,
			Coordinate coord) {
		for (long vectorSize = 500; vectorSize <= 20000; vectorSize += 500) {
			DaxpyKernel kernel = new DaxpyKernel();
			kernel.initialize(coord);
			kernel.setOptimization("-O3");
			kernel.setVectorSize(vectorSize);

			rooflineController
					.addRooflinePoint(kernel.getLabel(),
							Long.toString(vectorSize), kernel,
							kernel.getSuggestedOperation(),
							MemoryTransferBorder.LlcRamLines);
		}
	}

}
