package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import com.google.inject.Inject;
import java.io.*;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.MeasurementService;
import ch.ethz.ruediste.roofline.sharedEntities.Measurement;
import ch.ethz.ruediste.roofline.sharedEntities.MeasurementResult;
import ch.ethz.ruediste.roofline.sharedEntities.MeasurerSet;
import ch.ethz.ruediste.roofline.sharedEntities.Workload;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.SeqFFTfftwKernel;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.SeqFFTfftwSplitKernel;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.TscMeasurer;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.TscMeasurerOutput;

public class SeqFFTfftwSplitMeasurementController implements IMeasurementController  {

	public String getName() {
		return "seqFFTfftwSplit";
	}

	public String getDescription() {
		return "runs a sequential FFTW (split)";
	}
	
	@Inject
	MeasurementService measurementService;

	public void measure(String outputName) throws IOException  {
		
		FileInputStream fstream = new FileInputStream("sizes.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		
		
		while ((line = br.readLine()) != null) {
			
			if (line.isEmpty()) continue;

			int size = Integer.parseInt(line);
			
			Measurement measurement = new Measurement();
			Workload workload = new Workload();
			measurement.addWorkload(workload);
	
			MeasurerSet set = new MeasurerSet();
			TscMeasurer measurer = new TscMeasurer();
			set.setMainMeasurer(measurer);
			
			SeqFFTfftwSplitKernel kernel = new SeqFFTfftwSplitKernel();
			kernel.setBufferSize(size);
			kernel.setOptimization("-O3");
			kernel.setWarmData(true);
			kernel.setWarmCode(true);
			
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
