package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Performance;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
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

	public void measure(String outputName) throws IOException {
		QuantityCalculator<Performance> perfCalc = quantityMeasuringService
				.getPerformanceCalculator(Operation.CompInstr,
						ClockType.CoreCycles);

		IntroductionExampleKernel kernel = new IntroductionExampleKernel();
		kernel.setOptimization("-O3");
		kernel.setM(1024 * 1024);
		kernel.setN(4);
		kernel.setVariant(0);

		QuantityMap result = quantityMeasuringService.measureQuantities(kernel,
				10, perfCalc);

		System.out.printf("Variant: %d. Performance: %e\n",
				kernel.getVariant(), result.best(perfCalc).getValue());
	}

}
