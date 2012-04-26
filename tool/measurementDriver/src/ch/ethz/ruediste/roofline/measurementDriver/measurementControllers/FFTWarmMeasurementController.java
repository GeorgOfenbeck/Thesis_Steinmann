package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.zip;
import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;
import java.util.*;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

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
import ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

import com.google.inject.Inject;

public class FFTWarmMeasurementController implements IMeasurementController {

	public String getName() {
		return "FFTwarm";
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
		//measureDifferences(outputName);
		//measurePerformance(outputName);
	}

	@SuppressWarnings("unchecked")
	public void measureDifferences(String outputName) throws ExecuteException,
			IOException {
		DistributionPlot plot = new DistributionPlot();
		plot.setOutputName(outputName + "Diff")
				.setTitle("FFT - Warm and Cold Caches")
				.setxLabel("Buffer Size").setxUnit("1")
				.setyLabel("Transfer Difference").setyUnit("bytes").setLogY();//.setYRange(-1e6, 1e6);

		ParameterSpace space = new ParameterSpace();

		for (long i = 32; i < 1024 * 1024; i *= 2)
			space.add(bufferSizeAxis, i);

		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();

		addMklKernel(space, kernelNames);

		for (final Coordinate c : space.getAllPoints(kernelAxis, null)) {
			Coordinate base = c;
			Coordinate warmCode = c.getExtendedPoint(warmCodeAxis, true);
			Coordinate warmData = c.getExtendedPoint(warmDataAxis, true);
			Coordinate warmDataCode = warmData.getExtendedPoint(warmCodeAxis,
					true);

			Iterable<TransferredBytes> tbBase = measureTransferredBytes(base);

			ArrayList<Pair<Iterable<TransferredBytes>, String>> allSeries = new ArrayList<Pair<Iterable<TransferredBytes>, String>>();
			IterableUtils.addAll(allSeries,
					Pair.of(measureTransferredBytes(warmData), "Data"),
					Pair.of(measureTransferredBytes(warmCode), "Code"),
					Pair.of(measureTransferredBytes(warmDataCode), "DataCode")
					);

			for (Pair<Iterable<TransferredBytes>, String> series : allSeries) {

				Long matrixSize = c.get(bufferSizeAxis);

				for (Pair<TransferredBytes, TransferredBytes> pair : zip(
						tbBase,
						series.getLeft())) {

					plot.addValue(
							kernelNames.get(c.get(kernelAxis))
									+ series.getRight(),
							matrixSize,
							pair.getLeft().getValue()
									- pair.getRight().getValue()
							);
				}
			}
		}

		plotService.plot(plot);
	}

	@SuppressWarnings("unchecked")
	public void measurePerformance(String outputName) throws ExecuteException,
			IOException {
		DistributionPlot plot = new DistributionPlot();
		plot.setOutputName(outputName + "Performance")
				.setTitle("FFT - Warm and Cold Caches")
				.setxLabel("Problem Size").setxUnit("Complex Doubles")
				.setyLabel("Performance").setyUnit("Flop/Cycle").setLogX();//.setYRange(-1e6, 1e6);

		ParameterSpace space = new ParameterSpace();

		for (long i = 32; i < 1024 * 1024; i *= 2)
			space.add(bufferSizeAxis, i);

		space.add(kernelClassAxis, FFTmklKernel.class);
		space.add(optimizationAxis, "-O3");

		for (final Coordinate c : space.getAllPoints(kernelClassAxis, null)) {
			Coordinate base = c;
			Coordinate warmCode = c.getExtendedPoint(warmCodeAxis, true);
			Coordinate warmData = c.getExtendedPoint(warmDataAxis, true);
			Coordinate warmDataCode = warmData.getExtendedPoint(warmCodeAxis,
					true);

			addPerformances(plot, base);
			addPerformances(plot, warmCode);
			addPerformances(plot, warmData);
			addPerformances(plot, warmDataCode);

		}

		plotService.plot(plot);
	}

	/**
	 * @param plot
	 * @param coord
	 */
	private void addPerformances(DistributionPlot plot, Coordinate coord) {

		KernelBase kernel = KernelBase.create(coord);

		QuantityCalculator<Performance> calc = quantityMeasuringService
				.getPerformanceCalculator(kernel
						.getSuggestedOperation(), ClockType.CoreCycles);

		QuantityMap quantities = quantityMeasuringService
				.measureQuantities(kernel, 10, calc);

		System.out.println(kernel.getLabel());
		plot.addValues(kernel.getLabel(), coord.get(bufferSizeAxis),
				quantities.getStatistics(calc));
	}

	/**
	 * @param c
	 * @return
	 */
	public Iterable<TransferredBytes> measureTransferredBytes(final Coordinate c) {
		IMeasurementBuilder builder = getBuilder(c);

		QuantityCalculator<TransferredBytes> calc = quantityMeasuringService
				.getTransferredBytesCalculator(MemoryTransferBorder.LlcRamBus);
		QuantityMap quantities = quantityMeasuringService
				.measureQuantities(builder, 20).with("main", calc).get();

		return quantities.get(calc);
	}

	/**
	 * @param c
	 * @return
	 */
	public DescriptiveStatistics measurePerformance(final Coordinate c) {
		IMeasurementBuilder builder = getBuilder(c);

		QuantityCalculator<Performance> calc = quantityMeasuringService
				.getPerformanceCalculator(c.get(kernelAxis)
						.getSuggestedOperation(), ClockType.CoreCycles);

		QuantityMap quantities = quantityMeasuringService
				.measureQuantities(builder, 10).with("main", calc).get();

		return quantities.getStatistics(calc);
	}

	/**
	 * @param outputName
	 * @throws ExecuteException
	 * @throws IOException
	 */
	public void measureRoofline(String outputName) throws ExecuteException,
			IOException {
		rooflineController.setOutputName(outputName);
		rooflineController.setTitle("FFT - Warm and Cold Caches");
		rooflineController.addDefaultPeaks();
		rooflineController.getPlot()//.setAutoscaleX(true)
				.setAutoscaleY(true)
				.setKeyPosition(KeyPosition.BottomRight).setSameSizeConnection(
						SameSizeConnection.ByOperationalIntensity);

		DistributionPlot plot = new DistributionPlot();
		plot.setOutputName(outputName + "tb")
				.setTitle("FFT - Warm and Cold Caches")
				.setxLabel("Buffer Size").setxUnit("1")
				.setyLabel("min10(Memory Transfer)").setyUnit("bytes");

		ParameterSpace space = new ParameterSpace();
		space.add(warmCodeAxis, false);
		space.add(warmCodeAxis, true);
		space.add(warmDataAxis, false);
		space.add(warmDataAxis, true);

		for (long i = 128; i < 1024 * 1024; i *= 2)
			space.add(bufferSizeAxis, i);
		//space.add(bufferSizeAxis, 128L);

		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();

		addMklKernel(space, kernelNames);

		configuration.push();
		for (final Coordinate coordinate : space.getAllPoints(kernelAxis, null)) {
			if (coordinate.get(bufferSizeAxis) >= 128 * 1024)
				configuration
						.set(QuantityMeasuringService.numberOfRunsKey, 1);
			else
				configuration
						.set(QuantityMeasuringService.numberOfRunsKey, 100);

			System.out.println(coordinate);
			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			IMeasurementBuilder builder = getBuilder(coordinate);
			Long matrixSize = coordinate.get(bufferSizeAxis);

			if (kernel.getWarmCode() && kernel.getWarmData()
					&& matrixSize < 4096)
				continue;

			/*QuantityCalculator<TransferredBytes> calc = quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRamLines);

			QuantityMap quantities = quantityMeasuringService
					.measureQuantities(builder, 10).with("main", calc).get();

			

			DescriptiveStatistics stats = new DescriptiveStatistics();
			for (TransferredBytes tb : quantities.get(calc)) {
				stats.addValue(tb.getValue());
				if (stats.getN() >= 10) {
					plot.addValue(kernel.getLabel(), matrixSize,
							stats.getMin());
					stats.clear();
				}
			}*/

			RooflinePoint point = rooflineController
					.addRooflinePoint(kernel.getLabel(),
							matrixSize, builder,
							kernel.getSuggestedOperation(),
							MemoryTransferBorder.LlcRamLines);
			if (kernel.getWarmData() && !kernel.getWarmCode()
					&& matrixSize < 10000)
				point.setLabel(Long.toString(matrixSize));
		}
		configuration.pop();

		rooflineController.plot();
		//plotService.plot(plot);
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
	public void addBlockedKernel(ParameterSpace space,
			HashMap<KernelBase, String> kernelNames) {
		{
			MMMKernel kernel = new MMMKernel();
			kernel.setOptimization("-O3");
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blocked);
			kernel.setNb(50);
			kernel.setNu(2);
			kernel.setMu(2);
			kernel.setKu(2);

			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "MMM-Block");
		}
	}

	/**
	 * @param space
	 * @param kernelNames
	 */
	public void addMklKernel(ParameterSpace space,
			HashMap<KernelBase, String> kernelNames) {

		FFTmklKernel kernel = new FFTmklKernel();
		kernel.setOptimization("-O3");
		kernel.setNumThreads(1);
		space.add(kernelAxis, kernel);
		kernelNames.put(kernel, kernel.getLabel());
	}
}
