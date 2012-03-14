package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;
import java.util.Map;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

import com.google.inject.Inject;

public class MultiThreadMeasurementController implements IMeasurementController {

	public String getName() {
		return "multiThread";
	}

	public String getDescription() {
		return "runs a multithreaded measurement";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	public void measure(String outputName) throws IOException {
		final MMMKernel kernel = new MMMKernel();
		kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blas_Openblas);
		kernel.setMatrixSize(100);
		kernel.setOptimization("-O3");

		QuantityCalculator<OperationCount> calc = quantityMeasuringService
				.getOperationCountCalculator(Operation.CompInstr);

		IMeasurementBuilder builder = new IMeasurementBuilder() {

			public Measurement build(Map<String, MeasurerSet> sets) {
				Measurement measurement = new Measurement();
				Workload workload = new Workload();
				measurement.addWorkload(workload);
				workload.setKernel(kernel);
				workload.setMeasurerSet(sets.get("main"));
				return measurement;
			}
		};

		QuantityMap quantities = quantityMeasuringService
				.measureQuantities(builder, 1).with("main", calc).get();

		System.out.printf("OpCount: %e\n", quantities.min(calc).getValue());
	}

}