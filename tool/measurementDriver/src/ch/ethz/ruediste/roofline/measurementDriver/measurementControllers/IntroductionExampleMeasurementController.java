package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.KeyPosition;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Performance;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.RooflineService.PeakAlgorithm;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.IntroductionExampleKernel;

import com.google.inject.Inject;

public class IntroductionExampleMeasurementController implements
		IMeasurementController {

	public String getName() {
		return "intro";
	}

	public String getDescription() {
		return "generate plots for the introduction";
	}

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public RooflineController rooflineController;

	@Inject
	public Configuration configuration;

	public void measure(String outputName) throws IOException {
		configuration.set(QuantityMeasuringService.numberOfRunsKey, 20);

		rooflineController.setOutputName(outputName);
		rooflineController.getPlot().addPeakPerformance("Peak Performance",
				new Performance(1));
		rooflineController.addPeakThroughput("MemLoad", PeakAlgorithm.Load,
				MemoryTransferBorder.LlcRamBus);
		//rooflineController.addDefaultPeaks();
		rooflineController.setTitle("Introductory Example");
		rooflineController.getPlot().setKeyPosition(KeyPosition.NoKey)
				.setYRange(0.01, Double.NaN).setXRange(0.01, Double.NaN);
		measure(0);
		measure(1);
		rooflineController.plot();
	}

	/**
	 * @param variant
	 */
	protected void measure(int variant) {
		Operation operation = Operation.CompInstr;
		ClockType clockType = ClockType.CoreCycles;

		rooflineController.setClockType(clockType);
		QuantityCalculator<Performance> perfCalc = quantityMeasuringService
				.getPerformanceCalculator(operation,
						clockType);

		IntroductionExampleKernel kernel = new IntroductionExampleKernel();
		kernel.setOptimization("-O3");
		kernel.setM(1000000);
		kernel.setN(4);

		kernel.setVariant(variant);

		QuantityMap result = quantityMeasuringService.measureQuantities(kernel,
				perfCalc);

		String name = String.format("Variant %d", variant);
		rooflineController.addRooflinePoint(name,
				name, kernel,
				operation, MemoryTransferBorder.LlcRamLines);

		System.out.printf("Variant: %d. Performance: %f\n",
				kernel.getVariant(), result.best(perfCalc).getValue());
	}
}
