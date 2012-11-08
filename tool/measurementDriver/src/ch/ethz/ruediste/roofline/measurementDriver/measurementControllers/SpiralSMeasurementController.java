package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.KeyPosition;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.SystemInfoService;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.Axes;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.DaxpyKernel;

import com.google.inject.Inject;

public class SpiralSMeasurementController implements IMeasurementController {

	public String getName() {
		return "SpiralS";
	}

	public String getDescription() {
		return "Controller used for the SpiralS generated code";
	}

	@Inject
	public SystemInfoService systemInfoService;

	@Inject
	public RooflineController rooflineController;

	@Inject
	public Configuration configuration;

	public void measure(String outputName) throws IOException {
	}
	
	/*
	public void measure(String outputName) throws IOException {
		rooflineController.setTitle("Vector-Vector Multiplication");
		rooflineController.setOutputName(outputName);
		rooflineController.addDefaultPeaks();
		rooflineController.getPlot().setAutoscaleY(true)
				.setKeyPosition(KeyPosition.BottomRight).setAutoscaleX(true);

		ParameterSpace space = new ParameterSpace();
		space.add(DaxpyKernel.useMklAxis, true);
		space.add(DaxpyKernel.useMklAxis, false);
		space.add(Axes.numThreadsAxis, 1);
		space.add(Axes.numThreadsAxis, systemInfoService.getOnlineCPUs()
				.size());

		for (Coordinate coord : space) {
			addPoints(rooflineController, coord);
		}
		rooflineController.plot();
	}

	public void addPoints(RooflineController rooflineController,
			Coordinate coord) {
		configuration.push();

		for (long vectorSize = 500; vectorSize <= 10000 * 1000; vectorSize *= 2) {
			if (vectorSize > 1000 * 1000)
				configuration.set(QuantityMeasuringService.numberOfRunsKey, 1);
			else
				configuration
						.set(QuantityMeasuringService.numberOfRunsKey, 100);

			DaxpyKernel kernel = new DaxpyKernel();
			kernel.initialize(coord);
			kernel.setOptimization("-O3");
			kernel.setVectorSize(vectorSize);

			rooflineController
					.addRooflinePoint(kernel.getLabel(),
							vectorSize, kernel,
							kernel.getSuggestedOperation(),
							MemoryTransferBorder.LlcRamLines);

		}
		configuration.pop();
	}*/

}
