package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;

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
		MeasurementDescription measurement = new MeasurementDescription();
		WorkloadDescription workload = new WorkloadDescription();
		measurement.addWorkload(workload);

		MeasurerSet set = new MeasurerSet();
		TscMeasurerDescription measurer = new TscMeasurerDescription();
		set.setMainMeasurer(measurer);
		workload.setKernel(new DummyKernelDescription());
		workload.setMeasurerSet(set);

		MeasurementResult result = measurementService.measure(measurement, 10);

		System.out.println("results: ");
		for (TscMeasurerOutput output : result.getMeasurerOutputs(measurer)) {
			System.out.println(output.getTics());
		}
	}

}
