package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.addAll;

import java.io.IOException;
import java.util.ArrayList;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.SeriesPlot;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

import com.google.inject.Inject;

public class MMMOpIntensMeasurementController implements IMeasurementController {

	public String getName() {
		return "mmmOp";
	}

	public String getDescription() {
		return "generates a plot showing the operational intensity of the triple loop and blocked";
	}

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public PlotService plotService;

	public void measure(String outputName) throws IOException {
		SeriesPlot plot = new SeriesPlot();
		plot.setOutputName(outputName).setxLabel("Matrix Size")
				.setxUnit("Doubles").setyLabel("Operational Intensity")
				.setyUnit("Operations/Byte").setLog();

		addTriplePoints(plot);

		ArrayList<Integer> sizes = new ArrayList<Integer>();
		addAll(sizes, 100, 150, 200, 300, 350, 400, 500, 600, 700
		//, 1000
		//,1500, 2500
		);

		for (int size : sizes) {
			System.out.println(size);
			MMMKernel kernel = new MMMKernel();
			kernel.setOptimization("-O3");
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blocked_Restrict);
			kernel.setMatrixSize(size);
			kernel.setNb(50);
			kernel.setNu(2);
			kernel.setMu(2);
			kernel.setKu(2);

			OperationalIntensity opInt = quantityMeasuringService
					.measureOperationalIntensity(kernel, size < 200 ? 10 : 10,
							MemoryTransferBorder.LlcRamBus,
							kernel.getSuggestedOperation());
			plot.addValue(kernel.getLabel(), size, opInt.getValue());

			TransferredBytes memTrans = quantityMeasuringService
					.measureTransferredBytes(kernel, size < 200 ? 10 : 10,
							MemoryTransferBorder.LlcRamBus);
			plot.addValue(kernel.getLabel() + "th", size,
					2. * size * size * size / memTrans.getValue());
		}

		String thName = "Blocked Theoretical";
		plot.addValue(thName, 100, 100 / 16);
		plot.addValue(thName, 512, 512 / 16);
		plot.addValue(thName, 513, 50 / 4);
		plot.addValue(thName, 5243, 50 / 4);
		plot.addValue(thName, 5244, 50 / 8);
		plot.addValue(thName, 7000, 50 / 8);
		plotService.plot(plot);
	}

	/**
	 * @param plot
	 */
	protected void addTriplePoints(SeriesPlot plot) {
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		addAll(sizes, 100, 150, 200, 225, 250, 300, 325, 350, 400, 500, 700,
				1000);

		for (int size : sizes) {
			MMMKernel kernel = new MMMKernel();
			kernel.setOptimization("-O3");
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_TripleLoop);
			kernel.setMatrixSize(size);

			OperationalIntensity opInt = quantityMeasuringService
					.measureOperationalIntensity(kernel, size < 200 ? 10 : 10,
							MemoryTransferBorder.LlcRamBus,
							kernel.getSuggestedOperation());
			plot.addValue(kernel.getLabel(), size, opInt.getValue());
		}

		String thName = "Triple Theoretical";
		plot.addValue(thName, 100, 100 / 16);
		plot.addValue(thName, 512, 512 / 16);
		plot.addValue(thName, 513, 0.25);
		plot.addValue(thName, 7000, 0.25);
	}

}
