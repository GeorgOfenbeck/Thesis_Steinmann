package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.RooflinePlot.SameSizeConnection;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.ClockType;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.WhtKernel;

import com.google.inject.Inject;

public class WhtMeasurementController implements IMeasurementController {

	public String getName() {
		return "wht";
	}

	public String getDescription() {
		return "starts a WHT measurement";
	}

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public RooflineController rooflineController;

	@Inject
	public Configuration configuration;

	@Inject
	public PlotService plotService;

	public void measure(String outputName) throws IOException {
		rooflineController.addDefaultPeaks();
		rooflineController.setTitle("WHT");
		rooflineController.setOutputName(outputName);
		rooflineController
				.getPlot()
				.setXRange(Double.NEGATIVE_INFINITY, 1000)
				.setYRange(0.01, Double.POSITIVE_INFINITY)
				.setKeyPosition(KeyPosition.BottomRight)
				.setSameSizeConnection(
						SameSizeConnection.ByOperationalIntensity);

		DistributionPlot plotInt = new DistributionPlot();
		plotInt.setOutputName(outputName + "Int")
				.setTitle("WHT - Warm and Cold Caches")
				.setxLabel("Buffer Size").setxUnit("1")
				.setyLabel("Operational Intensity").setyUnit("1").setLogY()
				.setBoxWidth(0.5).setKeyPosition(KeyPosition.TopRight)
				.setYRange(Double.NEGATIVE_INFINITY, 1000);

		ParameterSpace space = new ParameterSpace();
		space.add(warmCodeAxis, false);
		space.add(warmCodeAxis, true);
		space.add(warmDataAxis, false);
		space.add(warmDataAxis, true);
		for (Coordinate coord : space) {
			for (int size = 7; size < 23; size++) {
				if (size < 20)
					configuration.set(QuantityMeasuringService.numberOfRunsKey,
							100);
				else
					configuration.set(QuantityMeasuringService.numberOfRunsKey,
							1);
				WhtKernel kernel = new WhtKernel();
				kernel.initialize(coord);
				kernel.setBufferSizeExp(size);

				if (kernel.getWarmCode() && kernel.getWarmData() && size < 1/*16*/)
					continue;

				if (kernel.getWarmData() && size < 1/*12*/)
					continue;

				QuantityCalculator<Performance> calcPerf = quantityMeasuringService
						.getPerformanceCalculator(
								kernel.getSuggestedOperation(),
								ClockType.CoreCycles);

				QuantityCalculator<OperationalIntensity> calcInt = quantityMeasuringService
						.getOperationalIntensityCalculator(
								MemoryTransferBorder.LlcRamLines,
								kernel.getSuggestedOperation());

				QuantityMap quantities = quantityMeasuringService
						.measureQuantities(kernel, calcInt, calcPerf);

				for (QuantityMap group : quantities.grouped(10)) {
					RooflinePoint point = rooflineController
							.addRooflinePoint(
									kernel.getLabel(),
									new RooflinePoint(
											size, group.best(calcInt), group
													.best(calcPerf)));

					if (kernel.getWarmData() && !kernel.getWarmCode()
							&& size < 20)
						point.setLabel(Long.toString(size));

					plotInt.addValue(kernel.getLabel(), size,
							group.best(calcInt).getValue());
				}

			}
		}
		rooflineController.plot();
		plotService.plot(plotInt);
	}

}
