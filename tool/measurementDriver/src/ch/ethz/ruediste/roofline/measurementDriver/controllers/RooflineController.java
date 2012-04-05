package ch.ethz.ruediste.roofline.measurementDriver.controllers;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.ExecuteException;
import org.apache.log4j.Logger;

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
	private static final Logger log = Logger
			.getLogger(RooflineController.class);

	@Inject
	public RooflineService rooflineService;

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	PlotService plotService;

	@Inject
	SystemInfoService systemInfoService;

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

	public void addRooflinePoint(String seriesName, String label,
			final KernelBase kernel, Operation operation,
			MemoryTransferBorder border) {

		IMeasurementBuilder builder = new IMeasurementBuilder() {

			public Measurement build(Map<Object, MeasurerSet> sets) {
				Measurement measurement = new Measurement();
				Workload workload = new Workload();
				workload.setKernel(kernel);
				workload.setMeasurerSet(sets.get("main"));
				measurement.addWorkload(workload);

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
				return measurement;
			}
		};

		addRooflinePoint(seriesName, label, builder, operation, border);
	}

	public void addRooflinePoint(String seriesName, String label,
			IMeasurementBuilder builder, Operation operation,
			MemoryTransferBorder border) {
		// get required calculators
		QuantityCalculator<TransferredBytes> tbCalculator = quantityMeasuringService
				.getTransferredBytesCalculator(border);
		QuantityCalculator<OperationCount> opCountCalculator = quantityMeasuringService
				.getOperationCountCalculator(operation);
		QuantityCalculator<Time> timeCalc = quantityMeasuringService
				.getExecutionTimeCalculator(clockType);

		// measure
		QuantityMap result = quantityMeasuringService
				.measureQuantities(builder).with(
						"main", timeCalc, opCountCalculator, tbCalculator)
				.get();

		// build derived quantities
		OperationalIntensity operationalIntensity = new OperationalIntensity(
				result.min(tbCalculator), result.min(opCountCalculator));

		Performance performance = new Performance(
				result.min(opCountCalculator), result.min(timeCalc));

		// create point
		RooflinePoint point = new RooflinePoint(label, operationalIntensity,
				performance);

		addRooflinePoint(seriesName, point);
	}

	public void addRooflinePoint(String seriesName, String label,
			KernelBase kernel, OperationCount operationCount,
			MemoryTransferBorder border) {

		TransferredBytes transferredBytes = quantityMeasuringService
				.measureTransferredBytes(kernel, border);
		Time time = quantityMeasuringService.measureExecutionTime(kernel,
				clockType);

		RooflinePoint point = new RooflinePoint(label,
				new OperationalIntensity(transferredBytes, operationCount),
				new Performance(operationCount, time));

		addRooflinePoint(seriesName, point);
	}

	public void addRooflinePoint(String seriesName, String label,
			KernelBase kernel, Operation operation,
			TransferredBytes transferredBytes) {
		Time time = quantityMeasuringService.measureExecutionTime(kernel,
				clockType);
		OperationCount operationCount = quantityMeasuringService
				.measureOperationCount(kernel, operation);
		RooflinePoint point = new RooflinePoint(label,
				new OperationalIntensity(transferredBytes, operationCount),
				new Performance(operationCount, time));

		addRooflinePoint(seriesName, point);
	}

	public void addRooflinePoint(String seriesName, String label,
			OperationCount opCount,
			TransferredBytes transferredBytes, Time executionTime) {

		RooflinePoint point = new RooflinePoint(label,
				new OperationalIntensity(transferredBytes, opCount),
				new Performance(opCount, executionTime));
		addRooflinePoint(seriesName, point);
	}

	/**
	 * @param seriesName
	 * @param point
	 */
	public void addRooflinePoint(String seriesName, RooflinePoint point) {
		log.info(String.format("Added Roofline Point %s-%s: %s", seriesName,
				point.getLabel(), point));
		plot.addPoint(seriesName, point);
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
		}

		switch (systemInfoService.getCpuType()) {
		case Core:
			addPeakThroughput("MemLoad", PeakAlgorithm.Load,
					MemoryTransferBorder.LlcRam);
		break;
		case Yonah:
			addPeakThroughput("MemLoad", PeakAlgorithm.Load,
					MemoryTransferBorder.LlcRam);

			addPeakThroughput("MemRand", PeakAlgorithm.RandomLoad,
					MemoryTransferBorder.LlcRam);
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
