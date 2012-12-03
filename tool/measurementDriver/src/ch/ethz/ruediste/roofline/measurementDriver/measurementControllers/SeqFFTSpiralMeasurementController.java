package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.inject.Inject;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.MeasurementService;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.TscMeasurer;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.TscMeasurerOutput;

public class SeqFFTSpiralMeasurementController implements IMeasurementController {

	public String getName() {
		return "seqFFTSpiral";
	}

	public String getDescription() {
		return "runs a sequential FFTW";
	}
	
	@Inject
	MeasurementService measurementService;

	public void measure(String outputName) throws NumberFormatException, IOException  {
		
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
			
			FFTSpiralKernel kernel = new FFTSpiralKernel();
			kernel.setBufferSize(size);
			kernel.setOptimization("-O3 -march=corei7-avx -fno-tree-vectorize");
			kernel.setWarmData(true);
			kernel.setWarmCode(true);
			
			
			workload.setKernel(kernel);
			workload.setMeasurerSet(set);
	
			MeasurementResult result = measurementService.measure(measurement, 100);
	
			long min = Long.MAX_VALUE;
			//System.out.println("size = \t" + size);
			for (TscMeasurerOutput output : result.getMeasurerOutputs(measurer)) {							
				if ( min > output.getTics().longValue() )
					min = output.getTics().longValue();
					
				//System.out.println(output.getTics());
			}
			System.out.println(min);
		}
		
	}

}
