package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.single;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.services.MeasurementService;

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
		for (int size = 1; size <= 256; size *= 2) {
			Measurement measurement = new Measurement();
			Workload workload = new Workload();
			measurement.addWorkload(workload);
			MeasurerSet measurerSet = new MeasurerSet();
			workload.setMeasurerSet(measurerSet);

			DiskIoKernel kernel = new DiskIoKernel();
			workload.setKernel(kernel);
			workload.setWarmCaches(true);

			ExecutionTimeMeasurer measurer = new ExecutionTimeMeasurer();
			measurerSet.setMainMeasurer(measurer);

			kernel.setFileSize(1024L * 1024L * size);
			kernel.setIterations(1);

			MeasurementResult result = measurementService.measure(measurement,
					1);

			long time = single(result.getMeasurerOutputs(measurer)).getUSecs();

			double transfer = size * kernel.getIterations();

			double performance = transfer * 1.0e6 / (double) time;

			System.out.printf("performance %d MB: %.3g MB/s\n", size,
					performance);
		}
	}

}
