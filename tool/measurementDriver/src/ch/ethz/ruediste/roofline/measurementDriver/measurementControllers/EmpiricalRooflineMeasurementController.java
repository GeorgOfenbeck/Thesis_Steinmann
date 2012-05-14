package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.KeyPosition;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Performance;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Throughput;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.RooflineService.PeakAlgorithm;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.EmpiricalRooflineKernel;

import com.google.inject.Inject;

public class EmpiricalRooflineMeasurementController implements
		IMeasurementController {

	public String getName() {
		return "Empirical_Roofline";
	}

	public String getDescription() {
		return "generate plots for validation";
	}

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public RooflineController rooflineController;

	@Inject
	public Configuration configuration;

	public void measure(String outputName) throws IOException {
		configuration.set(QuantityMeasuringService.numberOfRunsKey, 100);

		rooflineController.setOutputName(outputName);
		rooflineController.getPlot().addPeakPerformance("Peak Performance",
				new Performance(2));
		rooflineController.addPeakThroughput("MemLoad", PeakAlgorithm.Load,
				MemoryTransferBorder.LlcRamBus);
		//rooflineController.addDefaultPeaks();
		rooflineController.setTitle("Emperical Roofline");
		rooflineController.getPlot().setKeyPosition(KeyPosition.NoKey)
				.setYRange(0.01, Double.POSITIVE_INFINITY)
				.setXRange(0.01, Double.POSITIVE_INFINITY);
//		rooflineController.getPlot().setAutoscaleY(true)
//		.setKeyPosition(KeyPosition.BottomRight).setAutoscaleX(true);
		measure(0);
		
		rooflineController.plot();
	}

	/**
	 * @param variant
	 */
	protected void measure(int variant) {
		
		
		{
			long tflop = 1;
			EmpiricalRooflineKernel kernel = new EmpiricalRooflineKernel();
			kernel.setOptimization("-O3");
			kernel.setFlops(tflop);
			kernel.setTransfered_bytes(16);
			kernel.setDummy(0);
			rooflineController.addRooflinePoint("x",
					tflop, kernel,
					Operation.DoublePrecisionFlop, MemoryTransferBorder.LlcRamLines);

			
			
		}
		
	for (long tflop = 2; tflop < 400; tflop = tflop *2 )
	{			
		EmpiricalRooflineKernel kernel = new EmpiricalRooflineKernel();
		kernel.setOptimization("-O3");
		kernel.setFlops(tflop);
		kernel.setTransfered_bytes(16);
		kernel.setDummy(0);
		rooflineController.addRooflinePoint("x",
				tflop, kernel,
				Operation.DoublePrecisionFlop, MemoryTransferBorder.LlcRamLines);

	}
	}
}
