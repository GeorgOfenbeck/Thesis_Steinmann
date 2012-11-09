package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import com.google.inject.Inject;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.MeasurementService;
import ch.ethz.ruediste.roofline.sharedEntities.Measurement;
import ch.ethz.ruediste.roofline.sharedEntities.MeasurementResult;
import ch.ethz.ruediste.roofline.sharedEntities.MeasurerSet;
import ch.ethz.ruediste.roofline.sharedEntities.Workload;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.AlenFFTfftwKernel;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.SeqFFTfftwKernel;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.TscMeasurer;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.TscMeasurerOutput;

public class AlenFFTfftwMeasurementController implements IMeasurementController {

	public String getName() {
		return "AlenFFT";
	}

	public String getDescription() {
		return "Alen's generated FFTs";
	}

	@Inject
	MeasurementService measurementService;
	
	public void measure(String outputName) throws IOException {
		
		for (int size = 3; size <= 65; ++size) {

			Measurement measurement = new Measurement();
			Workload workload = new Workload();
			measurement.addWorkload(workload);
	
			MeasurerSet set = new MeasurerSet();
			TscMeasurer measurer = new TscMeasurer();
			set.setMainMeasurer(measurer);
			
			AlenFFTfftwKernel kernel = new AlenFFTfftwKernel();
			kernel.setBufferSize(size);
			kernel.setOptimization("-O3");
			
			workload.setKernel(kernel);
			workload.setMeasurerSet(set);
	
			MeasurementResult result = measurementService.measure(measurement, 10);
	
			System.out.println("size = \t" + size);
			for (TscMeasurerOutput output : result.getMeasurerOutputs(measurer)) {
				System.out.println(output.getTics());
			}
			System.out.println();
		}
		
	}

}
