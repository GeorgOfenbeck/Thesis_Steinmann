package ch.ethz.ruediste.roofline.measurementDriver.controllers;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.Operation;

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
		Add, Mul, ArithBalanced, Load, Store, MemBalanced,
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

	public void addRooflinePoint(String name, KernelDescriptionBase kernel,
			Operation operation, MemoryTransferBorder border) {

		RooflinePoint point = new RooflinePoint(name,
				quantityMeasuringService.measureOperationalIntensity(kernel,
						border, operation),
				quantityMeasuringService.measurePerformance(kernel, operation,
						clockType));
		log.info("Added Roofline Point: " + point);
		plot.addPoint(point);
	}

	public void addRooflinePoint(String name, KernelDescriptionBase kernel,
			OperationCount operationCount, MemoryTransferBorder border) {
		TransferredBytes transferredBytes = quantityMeasuringService
				.measureTransferredBytes(kernel, border);
		Time time = quantityMeasuringService.measureExecutionTime(kernel,
				clockType);
		RooflinePoint point = new RooflinePoint(name, new OperationalIntensity(
				transferredBytes, operationCount), new Performance(
				operationCount, time));
		log.info("Added Roofline Point: " + point);
		plot.addPoint(point);
	}

	public void addRooflinePoint(String name, KernelDescriptionBase kernel,
			Operation operation, TransferredBytes transferredBytes) {
		Time time = quantityMeasuringService.measureExecutionTime(kernel,
				clockType);
		OperationCount operationCount = quantityMeasuringService
				.measureOperationCount(kernel, operation);
		RooflinePoint point = new RooflinePoint(name, new OperationalIntensity(
				transferredBytes, operationCount), new Performance(
				operationCount, time));

		plot.addPoint(point);
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
}
