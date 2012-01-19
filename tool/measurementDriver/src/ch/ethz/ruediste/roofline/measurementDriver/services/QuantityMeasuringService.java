package ch.ethz.ruediste.roofline.measurementDriver.services;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.PmuRepository;

import com.google.inject.Inject;

public class QuantityMeasuringService {
	@Inject
	ValidatingMeasurementService validatingMeasurementService;

	@Inject
	PmuRepository pmuRepository;

	public enum Operation {
		SSE,
		x87,
		ALL,
	}

	public static final Axis<Operation> operationAxis = new Axis<Operation>(
			"operation");

	public enum MemoryTransferBorder {
		CpuL1,
		L1L2,
		L2L3,
		LlcRam,
	}

	public static final Axis<MemoryTransferBorder> memoryTransferBorderAxis = new Axis<MemoryTransferBorder>(
			"memoryTransferBorder");

	public enum ClockType {
		CoreCycles,
		ReferenceCycles,
		uSecs
	}

	public static final Axis<ClockType> clockTypeAxis = new Axis<ClockType>(
			"clockType");

	public enum Quantity {
		Performance,
		MemoryBandwidth,
		OperationCount,
		MemoryTransfer,
		ExecutionTime,
	}

	public static final Axis<Quantity> quantityAxis = new Axis<Quantity>(
			"quantity");

	public Performance measurePerformance(
			KernelDescriptionBase kernel, Operation operation,
			ClockType clockType) {
		return new Performance(measureOperationCount(kernel, operation),
				measureExecutionTime(kernel, clockType));
	}

	public Throughput measureThroughput(
			KernelDescriptionBase kernel, MemoryTransferBorder border,
			ClockType clockType) {
		return new Throughput(measureTransferredBytes(kernel, border),
				measureExecutionTime(kernel, clockType));
	}

	public OperationCount measureOperationCount(
			KernelDescriptionBase kernel, Operation operation) {

		// handle the allOperations case
		if (operation == Operation.ALL) {
			return new OperationCount(measureOperationCount(kernel,
					Operation.SSE).getValue()
					+
					measureOperationCount(kernel, Operation.x87).getValue());
		}

		// setup the measurer
		final PerfEventMeasurerDescription measurer = new PerfEventMeasurerDescription();
		switch (operation) {
		case ALL:
			throw new Error("should not happen");
		case SSE:
			measurer.addEvent("ops", pmuRepository.getAvailableEvent(
					"coreduo::SSE_COMP_INSTRUCTIONS_RETIRED"));
			break;
		case x87:
			measurer.addEvent("ops", pmuRepository.getAvailableEvent(
					"coreduo::FP_COMP_INSTR_RET",
					"core::FP_COMP_OPS_EXE"));
			break;

		}

		MeasurementResult result = measure(kernel, measurer);

		// get the output
		return new OperationCount(PerfEventMeasurerOutput.getStatistics("ops",
				result).getMin());

	}

	public TransferredBytes measureTransferredBytes(
			KernelDescriptionBase kernel, MemoryTransferBorder border) {

		// setup the measurer
		final PerfEventMeasurerDescription measurer = new PerfEventMeasurerDescription();
		switch (border) {

		case CpuL1:
		case L1L2:
		case L2L3:
			throw new Error("Not Supported");
		case LlcRam:
			measurer.addEvent("transfers", pmuRepository.getAvailableEvent(
					"core::BUS_TRANS_MEM",
					"coreduo::BUS_TRANS_MEM"));
			break;

		}

		MeasurementResult result = measure(kernel, measurer);

		// get the output
		return new TransferredBytes(PerfEventMeasurerOutput.getStatistics(
				"transfers", result)
				.getMin() * 64);

	}

	public Time measureExecutionTime(
			KernelDescriptionBase kernel, ClockType clockType) {

		// setup the measurer
		final PerfEventMeasurerDescription measurer = new PerfEventMeasurerDescription();
		switch (clockType) {

		case CoreCycles:
			measurer.addEvent("cycles", pmuRepository.getAvailableEvent(
					"core::UNHALTED_CORE_CYCLES",
					"coreduo::UNHALTED_CORE_CYCLES"));
			break;
		case ReferenceCycles:
		case uSecs:
			throw new Error("Not Supported");
		}

		MeasurementResult result = measure(kernel, measurer);

		// get the output
		return new Time(PerfEventMeasurerOutput.getStatistics("cycles", result)
				.getMin());

	}

	/**
	 * @param kernel
	 * @param measurer
	 * @return
	 */
	public MeasurementResult measure(KernelDescriptionBase kernel,
			final PerfEventMeasurerDescription measurer) {
		// setup the measurement
		MeasurementDescription measurement = new MeasurementDescription();
		measurement.setKernel(kernel);
		measurement.setScheme(new SimpleMeasurementSchemeDescription());
		measurement.setMeasurer(measurer);

		// measure
		MeasurementResult result = validatingMeasurementService.measure(
				measurement, 10);
		return result;
	}

	public ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity measure(
			KernelDescriptionBase kernel,
			Coordinate measurementPoint) {
		switch (measurementPoint.get(quantityAxis)) {
		case ExecutionTime:
			return measureExecutionTime(kernel,
					measurementPoint.get(clockTypeAxis));
		case MemoryBandwidth:
			return measureThroughput(kernel,
					measurementPoint.get(memoryTransferBorderAxis),
					measurementPoint.get(clockTypeAxis));
		case MemoryTransfer:
			return measureTransferredBytes(kernel,
					measurementPoint.get(memoryTransferBorderAxis));
		case OperationCount:
			return measureOperationCount(kernel,
					measurementPoint.get(operationAxis));
		case Performance:
			return measurePerformance(kernel,
					measurementPoint.get(operationAxis),
					measurementPoint.get(clockTypeAxis));
		}
		throw new Error("should not happen");
	}
}
