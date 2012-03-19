package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.single;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.MeasurementService;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.DiskIoKernel;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.ExecutionTimeMeasurer;

import com.google.inject.Inject;

public class DiskIoMeasurementController implements IMeasurementController {

	public String getName() {
		return "diskio";
	}

	public String getDescription() {
		return "run the DiskIo kernel with multiple file sizes to show the disk cache";
	}

	@Inject
	MeasurementService measurementService;

	public void measure(String outputName) throws IOException {
		for (int size = 1; size <= 16; size *= 2) {
			int iterations = 1;

			Measurement measurement = new Measurement();
			Workload workload = new Workload();
			measurement.addWorkload(workload);

			DiskIoKernel kernel = new DiskIoKernel();
			workload.setKernel(kernel);
			workload.setMeasurerSet(new MeasurerSet());

			kernel.setFileSize(1024L * 1024L * size);
			kernel.setIterations(iterations);

			MeasurerSet measurerSet = new MeasurerSet();
			ExecutionTimeMeasurer measurer = new ExecutionTimeMeasurer();
			measurerSet.setMainMeasurer(measurer);
			workload.setMeasurerSet(measurerSet);
			workload.setWarmData(true);
			workload.setWarmCode(true);

			MeasurementResult result = measurementService.measure(measurement,
					1);

			long time = single(result.getMeasurerOutputs(measurer)).getUSecs();

			double transfer = size * iterations;

			double performance = transfer * 1.0e6 / (double) time;

			System.out.printf("performance %d MB: %.3g MB/s\n", size,
					performance);
		}
	}

	/**
	 * @param measurement
	 * @param size
	 * @param iterations
	 * @return
	 */
	public Workload createDiskWorkload(int size, int iterations) {
		Workload workload = new Workload();

		DiskIoKernel kernel = new DiskIoKernel();
		workload.setKernel(kernel);
		workload.setMeasurerSet(new MeasurerSet());

		kernel.setFileSize(1024L * 1024L * size);
		kernel.setIterations(iterations);

		return workload;
	}

}
