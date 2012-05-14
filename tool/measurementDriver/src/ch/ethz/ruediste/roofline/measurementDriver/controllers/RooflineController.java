package ch.ethz.ruediste.roofline.measurementDriver.controllers;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.ExecuteException;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.RooflineService.PeakAlgorithm;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.actions.CreateMeasurerOnThreadAction;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.*;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.WorkloadEventPredicate.WorkloadEventEnum;

import com.google.inject.Inject;

public class RooflineController {
	public static final ConfigurationKey<Boolean> measureMultiThreadedKey = ConfigurationKey
			.Create(Boolean.class, "measureMultiThreaded",
					"if set to true, take other threads into account", true);
	private static final Logger log = Logger
			.getLogger(RooflineController.class);

	@Inject
	public RooflineService rooflineService;

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public PlotService plotService;

	@Inject
	public SystemInfoService systemInfoService;

	@Inject
	public Configuration configuration;

	private final RooflinePlot plot = new RooflinePlot();

	private ClockType clockType = ClockType.CoreCycles;

	public RooflineController() {
		plot.setOutputName("roofline");
		plot.setTitle("A Roofline Plot");
		plot.setxLabel("Operational Intensity");
		plot.setxUnit("Flop/Byte");
		plot.setyLabel("Performance");
		plot.setyUnit("Flop/Cycle");
	}

	public void addPeakPerformance(String name, PeakAlgorithm algorithm,
			InstructionSet instructionSet) {
		Performance performance = rooflineService.measurePeakPerformance(
				algorithm, instructionSet, getClockType());

		plot.addPeakPerformance(name, performance);
	}

	public void addPeakThroughput(String name, PeakAlgorithm algorithm,
			MemoryTransferBorder border) {
		Throughput throughput = rooflineService.measurePeakThroughput(
				algorithm, border, getClockType());

		plot.addPeakThroughput(name, throughput);
	}

	public RooflinePoint addRooflinePoint(String seriesName, long problemSize,
			final KernelBase kernel, Operation operation,
			MemoryTransferBorder border) {

		IMeasurementBuilder builder = new IMeasurementBuilder() {

			public Measurement build(Map<Object, MeasurerSet> sets) {
				Measurement measurement = new Measurement();
				Workload workload = new Workload();
				workload.setKernel(kernel);
				workload.setMeasurerSet(sets.get("main"));
				measurement.addWorkload(workload);

				if (configuration.get(measureMultiThreadedKey)) {
					// create predicates
					WorkloadEventPredicate startPredicate = new WorkloadEventPredicate(
							workload,
							WorkloadEventEnum.KernelStart);

					WorkloadEventPredicate stopPredicate = new WorkloadEventPredicate(
							workload,
							WorkloadEventEnum.KernelStop);

					// configure create measurer action
					{
						CreateMeasurerOnThreadAction action = new CreateMeasurerOnThreadAction();
						measurement.addRule(new Rule(startPredicate, action));

						action.setMeasurerSet(sets.get("main"));

						action.setStartPredicate(null);
						action.setStopPredicate(stopPredicate);
						action.setReadPredicate(stopPredicate);
						action.setDisposePredicate(stopPredicate);
					}
				}
				return measurement;
			}
		};

		return addRooflinePoint(seriesName, problemSize, builder, operation,
				border);
	}

	public RooflinePoint addRooflinePoint(String seriesName, long problemSize,
			IMeasurementBuilder builder, Operation operation,
			MemoryTransferBorder border) {
		// get required calculators
		QuantityCalculator<TransferredBytes> tbCalculator = quantityMeasuringService
				.getTransferredBytesCalculator(border);
		QuantityCalculator<OperationCount> opCountCalculator = quantityMeasuringService
				.getOperationCountCalculator(operation);
		QuantityCalculator<Time> timeCalc = quantityMeasuringService
				.getTimeCalculator(clockType);

		// measure
		QuantityMap result = quantityMeasuringService
				.measureQuantities(builder).with(
						"main", timeCalc, opCountCalculator, tbCalculator)
				.get();

		RooflinePoint lastAddedPoint = null;
		for (QuantityMap map : result.grouped(10)) {
			// build derived quantities
			OperationalIntensity operationalIntensity = new OperationalIntensity(
					map.best(tbCalculator),
					map.best(opCountCalculator));

			Performance performance = new Performance(
					map.best(opCountCalculator), map.best(timeCalc));

			// create point
			RooflinePoint point = new RooflinePoint(problemSize,
					operationalIntensity,
					performance);

			lastAddedPoint = addRooflinePoint(seriesName, point);
		}

		return lastAddedPoint;
	}

	public RooflinePoint addRooflinePoint(String seriesName, long problemSize,
			KernelBase kernel, OperationCount operationCount,
			MemoryTransferBorder border) {

		QuantityCalculator<TransferredBytes> tbCalculator = quantityMeasuringService
				.getTransferredBytesCalculator(border);
		QuantityCalculator<Time> timeCalc = quantityMeasuringService
				.getTimeCalculator(clockType);

		// measure
		QuantityMap result = quantityMeasuringService
				.measureQuantities(kernel, timeCalc, tbCalculator);

		RooflinePoint point = new RooflinePoint(problemSize,
				new OperationalIntensity(result.best(tbCalculator),
						operationCount),
				new Performance(operationCount, result.best(timeCalc)));

		addRooflinePoint(seriesName, point);
		return point;
	}

	public RooflinePoint addRooflinePoint(String seriesName, long problemSize,
			KernelBase kernel, Operation operation,
			TransferredBytes transferredBytes) {
		QuantityCalculator<Time> timeCalc = quantityMeasuringService
				.getTimeCalculator(clockType);
		QuantityCalculator<OperationCount> opCountCalc = quantityMeasuringService
				.getOperationCountCalculator(operation);

		QuantityMap result = quantityMeasuringService.measureQuantities(kernel,
				opCountCalc, timeCalc);
		OperationCount operationCount = result.best(opCountCalc);
		Time time = result.best(timeCalc);

		RooflinePoint point = new RooflinePoint(problemSize,
				new OperationalIntensity(transferredBytes, operationCount),
				new Performance(operationCount, time));

		addRooflinePoint(seriesName, point);
		return point;
	}

	public RooflinePoint addRooflinePoint(String seriesName, long problemSize,
			OperationCount opCount,
			TransferredBytes transferredBytes, Time executionTime) {

		RooflinePoint point = new RooflinePoint(problemSize,
				new OperationalIntensity(transferredBytes, opCount),
				new Performance(opCount, executionTime));
		addRooflinePoint(seriesName, point);
		return point;
	}

	public RooflinePoint addRooflinePoint(String seriesName, RooflinePoint point) {
		log.info(String.format("Added Roofline Point %s-%s: %s", seriesName,
				point.getProblemSize(), point));
		return plot.addPoint(seriesName, point);
	}

	public ClockType getClockType() {
		return clockType;
	}

	public void setClockType(ClockType clockType) {
		this.clockType = clockType;
	}

	public void plot() throws ExecuteException, IOException {
		plotService.plot(plot);
	}

	public void setTitle(String title) {
		plot.setTitle(title);
	}

	public void addDefaultPeaks() {

		switch (systemInfoService.getCpuType()) {
		case Core:
			plot.addPeakPerformance("SSE 2x", new Performance(8));
			plot.addPeakPerformance("Balanced SSE", new Performance(4));
			plot.addPeakPerformance("Balanced Scalar", new Performance(2));
		break;
		case Yonah:
			plot.addPeakPerformance("Single Core", new Performance(1));
			plot.addPeakPerformance("Dual Core", new Performance(2));
		//addPeakPerformance("ADD", PeakAlgorithm.Add, InstructionSet.SSE);
		//addPeakPerformance("MUL", PeakAlgorithm.Mul, InstructionSet.SSE);
		break;
		case SandyBridge: //2DO - replace by measured results 
			plot.addPeakPerformance("Balanced Scalar", new Performance(2));
			plot.addPeakPerformance("AVX", new Performance(8));
			plot.addPeakPerformance("2*AVX", new Performance(16));
			break;
		case SandyBridgeExtreme:
			plot.addPeakPerformance("Balanced Scalar", new Performance(2));
			plot.addPeakPerformance("AVX", new Performance(8));
			plot.addPeakPerformance("6*AVX", new Performance(48));
			break;
		}

		switch (systemInfoService.getCpuType()) {
		case Core:
			addPeakThroughput("MemLoad", PeakAlgorithm.Load,
					MemoryTransferBorder.LlcRamBus);
			addPeakThroughput("MemRand", PeakAlgorithm.RandomLoad,
					MemoryTransferBorder.LlcRamBus);
		break;
		case Yonah:
			//plot.addPeakThroughput("Theoretical", new Throughput(2.8));
			addPeakThroughput("MemLoad", PeakAlgorithm.Load,
					MemoryTransferBorder.LlcRamBus);

			addPeakThroughput("MemRand", PeakAlgorithm.RandomLoad,
					MemoryTransferBorder.LlcRamBus);
		break;
		
		
		//GO: Sandybridge doesnt work yet - Operational Intensity is wrong
		case SandyBridge:
			addPeakThroughput("MemLoad", PeakAlgorithm.Load,
					MemoryTransferBorder.LlcRamBus);
			break;				
		case SandyBridgeExtreme:
			addPeakThroughput("MemLoad", PeakAlgorithm.Load,
					MemoryTransferBorder.LlcRamBus);
			break;
			
		
		}
	}

	public void setOutputName(String outputName) {
		plot.setOutputName(outputName);

	}

	public RooflinePlot getPlot() {
		return plot;
	}
}
