package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.Operation;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.SfoKernel;

import com.google.inject.Inject;

public class SfoMeasurementController implements IMeasurementController {

	public String getName() {
		return "sfo";
	}

	public String getDescription() {
		return "runs the sfo kernel";
	}

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	public void measure(String outputName) throws IOException {

		measure(0);
		measure(1);
	}

	/**
	 * @param calc
	 * @param useSatoru
	 */
	private void measure(int useSatoru) {
		QuantityCalculator<OperationCount> calc = quantityMeasuringService
				.getOperationCountCalculator(Operation.DoublePrecisionFlop);

		SfoKernel kernel = new SfoKernel();
		kernel.setControlFile("/home/ruedi/svn/azuagarg_thesis/test/genrmf_data/out_a8_b5_n320.txt");
		kernel.setUseSatoru(useSatoru);

		QuantityMap result = quantityMeasuringService.measureQuantities(kernel,
				1, calc);

		System.out
				.printf("UseSatory: %d Operation count: %e\f", useSatoru,
						result.best(calc).getValue());
	}

}
