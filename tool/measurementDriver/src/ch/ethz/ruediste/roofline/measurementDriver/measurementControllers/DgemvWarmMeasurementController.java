package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;
import java.util.ArrayList;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.RooflinePlot.SameSizeConnection;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.Axes;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;

import com.google.inject.Inject;

public class DgemvWarmMeasurementController implements IMeasurementController {

	public String getName() {
		return "dgemvWarm";
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
		rooflineController.getPlot().setSameSizeConnection(
				SameSizeConnection.ByPerformance);

		ParameterSpace space = new ParameterSpace();
		space.add(DaxpyKernel.useMklAxis, true);
		//space.add(DaxpyKernel.useMklAxis, false);
		space.add(Axes.numThreadsAxis, 1);
		/*space.add(Axes.numThreadsAxis, systemInfoService.getOnlineCPUs()
				.size());*/
		space.add(warmCodeAxis, false);
		space.add(warmCodeAxis, true);
		space.add(warmDataAxis, false);
		space.add(warmDataAxis, true);

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
		ArrayList<Long> matrixSizes = new ArrayList<Long>();
		matrixSizes.add(100L);
		matrixSizes.add(200L);
		matrixSizes.add(300L);
		matrixSizes.add(400L);
		for (long matrixSize = 500; matrixSize <= 6000; matrixSize += 500)
			matrixSizes.add(matrixSize);

		for (long matrixSize : matrixSizes) {
			if (matrixSize > 2000) {
				configuration.set(QuantityMeasuringService.numberOfRunsKey, 1);
			}
			else {
				configuration
						.set(QuantityMeasuringService.numberOfRunsKey, 100);
			}
			DgemvKernel kernel = new DgemvKernel();
			kernel.initialize(coord);
			kernel.setOptimization("-O3");
			kernel.setMatrixSize(matrixSize);

			rooflineController
					.addRooflinePoint(kernel.getLabelOverride(),
							matrixSize, kernel,
							kernel.getSuggestedOperation(),
							MemoryTransferBorder.LlcRamLines);
		}
		configuration.pop();
	}
}
