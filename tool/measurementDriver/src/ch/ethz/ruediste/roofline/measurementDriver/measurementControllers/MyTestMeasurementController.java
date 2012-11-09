package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.MeasurementService;
import ch.ethz.ruediste.roofline.sharedEntities.Measurement;
import ch.ethz.ruediste.roofline.sharedEntities.MeasurementResult;
import ch.ethz.ruediste.roofline.sharedEntities.MeasurerSet;
import ch.ethz.ruediste.roofline.sharedEntities.Workload;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.DummyKernel;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.TestKernel;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.TscMeasurer;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.TscMeasurerOutput;

import com.google.inject.Inject;

public class MyTestMeasurementController implements IMeasurementController {
	
	public String getName() {
		return "myController";
	}

	public String getDescription() {
		return "description";
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
		TestKernel kernel = new TestKernel();
		kernel.setVectorSize(1000);
		workload.setKernel(kernel);
		workload.setMeasurerSet(set);

		MeasurementResult result = measurementService.measure(measurement, 10);

		System.out.println("results: ");
		for (TscMeasurerOutput output : result.getMeasurerOutputs(measurer)) {
			System.out.println(output.getTics());
		}
	}


}
