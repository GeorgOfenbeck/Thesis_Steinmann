package ch.ethz.ruediste.roofline.measurementDriver.controllers;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;

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

	private RooflinePlot plot = new RooflinePlot();

	private ClockType clockType = ClockType.CoreCycles;

	public enum Algorithm {
		Add, Mul, ArithBalanced, Load, Store, MemBalanced, RandomLoad,
	}

	public RooflineController() {
		plot.setOutputName("roofline");
		plot.setTitle("A Roofline Plot");
		plot.setxLabel("Operational Intensity");
		plot.setxUnit("flop/Byte");
		plot.setyLabel("Performance");
		plot.setyUnit("flop/cycle");
	}

	public void addPeakPerformance(String name, Algorithm algorithm,
			InstructionSet instructionSet) {
		Performance performance = rooflineService.measurePeakPerformance(
				algorithm, instructionSet, getClockType());

		plot.addPeakPerformance(name, performance);
	}

	public void addPeakThroughput(String name, Algorithm algorithm,
			MemoryTransferBorder border) {
		Throughput throughput = rooflineService.measurePeakThroughput(
				algorithm, border, getClockType());

		plot.addPeakThroughput(name, throughput);
	}

	public void addRooflinePoint(String seriesName, String label,
			KernelBase kernel, Operation operation, MemoryTransferBorder border) {

		// get required calculators
		QuantityCalculator<TransferredBytes> tbCalculator = quantityMeasuringService
				.getTransferredBytesCalculator(border);
		QuantityCalculator<OperationCount> opCountCalculator = quantityMeasuringService
				.getOperationCountCalculator(operation);
		QuantityCalculator<Time> timeCalc = quantityMeasuringService
				.getExecutionTimeCalculator(clockType);

		// measure 
		QuantityMap result = quantityMeasuringService.measureQuantities(kernel,
				timeCalc, opCountCalculator, tbCalculator);

		// build derived quantities
		OperationalIntensity operationalIntensity = new OperationalIntensity(
				result.min(tbCalculator), result.min(opCountCalculator));

		Performance performance = new Performance(
				result.min(opCountCalculator), result.min(timeCalc));

		// create point
		RooflinePoint point = new RooflinePoint(label, operationalIntensity,
				performance);

		// log output
		log.info(String.format("Added Roofline Point %s-%s: %s", seriesName,
				label, point));

		// add point
		plot.addPoint(seriesName, point);
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

		log.info(String.format("Added Roofline Point %s-%s: %s", seriesName,
				label, point));
		plot.addPoint(seriesName, point);
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

		log.info(String.format("Added Roofline Point %s-%s: %s", seriesName,
				label, point));

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
		addPeakPerformance("ADD", Algorithm.Add, InstructionSet.SSE);
		addPeakPerformance("MUL", Algorithm.Mul, InstructionSet.SSE);

		addPeakThroughput("MemLoad", Algorithm.Load,
				MemoryTransferBorder.LlcRam);

		addPeakThroughput("MemRandom", Algorithm.RandomLoad,
				MemoryTransferBorder.LlcRam);
	}
}
