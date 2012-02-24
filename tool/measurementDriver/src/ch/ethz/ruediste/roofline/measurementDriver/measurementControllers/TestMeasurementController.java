package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.entities.MeasurementResult;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.DummyKernel;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.*;

import com.google.inject.Inject;

public class TestMeasurementController implements IMeasurementController {

	public String getName() {
		return "test";
	}

	public String getDescription() {
		return "simple measurement controller for refactoring";
	}

	@Inject
	MeasurementService measurementService;

	public void measure(String outputName) throws IOException {
		Measurement measurement = new Measurement();
		Workload workload = new Workload();
		measurement.addWorkload(workload);

		MeasurerSet set = new MeasurerSet();
		TscMeasurer measurer = new TscMeasurer();
		set.setMainMeasurer(measurer);
		workload.setKernel(new DummyKernel());
		workload.setMeasurerSet(set);

		MeasurementResult result = measurementService.measure(measurement, 10);

		System.out.println("results: ");
		for (TscMeasurerOutput output : result.getMeasurerOutputs(measurer)) {
			System.out.println(output.getTics());
		}
	}

}
