package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.appControllers.MeasurementAppController;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;

import com.google.inject.Inject;

public class CpuMigrationMeasurementController implements
		IMeasurementController {

	public String getName() {
		return "cpuMigration";
	}

	public String getDescription() {
		return "checks if the CPU migration detection works";
	}

	@Inject
	MeasurementAppController measurementAppController;

	@Inject
	Configuration configuration;

	public void measure(String outputName) throws IOException {
		CpuMigratingKernelDescription kernel = new CpuMigratingKernelDescription();
		kernel.setTargetCpu(1);

		MeasurementDescription measurement = new MeasurementDescription();
		WorkloadDescription workload = new WorkloadDescription();
		workload.setKernel(kernel);
		workload.setCpu(0);

		MeasurementResult result = measurementAppController.measure(
				measurement, 1);

		PerfEventMeasurerDescription validationMeasurer = measurement
				.getValidationData().getPerfEventMeasurer();

		System.out.println("Number of CPU Migrations: "
				+ validationMeasurer.getBigIntegers("cpuMigrations", result));
	}

}
