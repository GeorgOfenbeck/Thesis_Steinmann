package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.ExecuteException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.RooflinePlot.SameSizeConnection;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

import com.google.inject.Inject;

public class MMMWarmMeasurementController implements IMeasurementController {

	public String getName() {
		return "MMMwarm";
	}

	public String getDescription() {
		return "comparison between warm and cold measurements";

	}

	@Inject
	RooflineController rooflineController;

	@Inject
	Configuration configuration;

	@Inject
	PlotService plotService;

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	public void measure(String outputName) throws IOException {
		measureRoofline(outputName);
	}

	/**
	 * @param outputName
	 * @throws ExecuteException
	 * @throws IOException
	 */
	public void measureRoofline(String outputName) throws ExecuteException,
			IOException {
		rooflineController.setOutputName(outputName);
		rooflineController.setTitle("MMM - Warm and Cold Caches");
		rooflineController.addDefaultPeaks();
		rooflineController
				.getPlot()
				//.setAutoscaleX(true)
				.setKeyPosition(KeyPosition.BottomRight)
				.setXRange(Double.NEGATIVE_INFINITY, 1000)
				.setSameSizeConnection(
						SameSizeConnection.ByOperationalIntensity);

		DistributionPlot plotInt = new DistributionPlot();
		plotInt.setOutputName(outputName + "Int")
				.setTitle("MMM - Warm and Cold Caches")
				.setxLabel("Matrix Size").setxUnit("1")
				.setyLabel("Operational Intensity").setyUnit("1").setLogY()
				.setBoxWidth(10).setKeyPosition(KeyPosition.TopRight)
				.setYRange(Double.NEGATIVE_INFINITY, 10000);

		ParameterSpace space = new ParameterSpace();
		space.add(warmCodeAxis, false);
		space.add(warmCodeAxis, true);
		space.add(warmDataAxis, false);
		space.add(warmDataAxis, true);

		space.add(matrixSizeAxis, 25L);
		space.add(matrixSizeAxis, 50L);
		space.add(matrixSizeAxis, 100L);
		space.add(matrixSizeAxis, 150L);
		for (long i = 200; i < 700; i += 100)
			space.add(matrixSizeAxis, i);

		addMklKernel(space);

		configuration.push();
		for (final Coordinate coordinate : space.getAllPoints(kernelAxis, null)) {
			if (coordinate.get(matrixSizeAxis) > 400)
				configuration.set(QuantityMeasuringService.numberOfRunsKey, 1);
			else
				configuration
						.set(QuantityMeasuringService.numberOfRunsKey, 100);
			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			IMeasurementBuilder builder = getBuilder(coordinate);
			Long matrixSize = coordinate.get(matrixSizeAxis);

			QuantityCalculator<Performance> calcPerf = quantityMeasuringService
					.getPerformanceCalculator(kernel.getSuggestedOperation(),
							ClockType.CoreCycles);

			QuantityCalculator<OperationalIntensity> calcInt = quantityMeasuringService
					.getOperationalIntensityCalculator(
							MemoryTransferBorder.LlcRamLines,
							kernel.getSuggestedOperation());

			QuantityMap quantities = quantityMeasuringService
					.measureQuantities(builder)
					.with("main", calcInt, calcPerf).get();

			for (QuantityMap group : quantities.grouped(10)) {
				plotInt.addValue(kernel.getLabel(), matrixSize,
						group.best(calcInt).getValue());

				RooflinePoint point = rooflineController
						.addRooflinePoint(
								kernel.getLabel(),
								new RooflinePoint(
										matrixSize, group.best(calcInt), group
												.best(calcPerf)));

				if (kernel.getWarmData() && !kernel.getWarmCode()
						&& matrixSize < 300)
					point.setLabel(Long.toString(matrixSize));
			}

		}
		configuration.pop();

		rooflineController.plot();
		plotService.plot(plotInt);
	}

	/**
	 * @param coordinate
	 * @return
	 */
	public IMeasurementBuilder getBuilder(final Coordinate coordinate) {
		IMeasurementBuilder builder = new IMeasurementBuilder() {

			public Measurement build(Map<Object, MeasurerSet> sets) {
				Measurement measurement = new Measurement();
				Workload workload = new Workload();
				workload.initialize(coordinate);
				workload.setMeasurerSet(sets.get("main"));
				measurement.addWorkload(workload);
				return measurement;
			}
		};
		return builder;
	}

	/**
	 * @param space
	 * @param kernelNames
	 */
	public void addBlockedKernel(ParameterSpace space) {
		{
			MMMKernel kernel = new MMMKernel();
			kernel.setOptimization("-O3");
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blocked);
			kernel.setNb(50);
			kernel.setNu(2);
			kernel.setMu(2);
			kernel.setKu(2);

			space.add(kernelAxis, kernel);
		}
	}

	/**
	 * @param space
	 * @param kernelNames
	 */
	public void addMklKernel(ParameterSpace space) {
		{
			MMMKernel kernel = new MMMKernel();
			kernel.setOptimization("-O3");
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blas);
			kernel.setUseMkl(true);
			space.add(kernelAxis, kernel);
		}
	}
}
