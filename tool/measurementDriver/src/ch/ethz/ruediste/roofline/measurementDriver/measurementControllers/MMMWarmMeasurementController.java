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
		rooflineController.getPlot().setAutoscaleX(true)
				.setKeyPosition(KeyPosition.BottomRight);

		DistributionPlot plotTb = new DistributionPlot();
		plotTb.setOutputName(outputName + "tb")
				.setTitle("MMM - Warm and Cold Caches")
				.setxLabel("Matrix Size").setxUnit("1")
				.setyLabel("Memory Transfer Volume").setyUnit("bytes");

		DistributionPlot plotInt = new DistributionPlot();
		plotInt.setOutputName(outputName + "Int")
				.setTitle("MMM - Warm and Cold Caches")
				.setxLabel("Buffer Size").setxUnit("1")
				.setyLabel("Operational Intensity").setyUnit("1").setLogY()
				.setBoxWidth(5);

		ParameterSpace space = new ParameterSpace();
		space.add(warmCodeAxis, false);
		space.add(warmCodeAxis, true);
		space.add(warmDataAxis, false);
		space.add(warmDataAxis, true);

		space.add(matrixSizeAxis, 50L);
		for (long i = 100; i < 700; i += 100)
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

			QuantityCalculator<Throughput> calcTb = quantityMeasuringService
					.getThroughputCalculator(MemoryTransferBorder.LlcRamBus,
							ClockType.CoreCycles);
			QuantityCalculator<OperationalIntensity> calcInt = quantityMeasuringService
					.getOperationalIntensityCalculator(
							MemoryTransferBorder.LlcRamBus,
							kernel.getSuggestedOperation());

			QuantityMap quantities = quantityMeasuringService
					.measureQuantities(builder)
					.with("main", calcTb, calcInt).get();

			for (QuantityMap group : quantities.grouped(10)) {
				plotTb.addValue(kernel.getLabel(), matrixSize,
						group.best(calcTb).getValue());
				plotInt.addValue(kernel.getLabel(), matrixSize,
						group.best(calcInt).getValue());
			}

			rooflineController
					.addRooflinePoint(kernel.getLabel(),
							matrixSize, builder,
							kernel.getSuggestedOperation(),
							MemoryTransferBorder.LlcRamBus);
		}
		configuration.pop();

		rooflineController.plot();
		plotService.plot(plotTb);
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
