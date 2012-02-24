package ch.ethz.ruediste.roofline.measurementDriver.services;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.entities.MeasurementResult;
import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.SystemInfoRepository;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.*;

import com.google.inject.Inject;

public class QuantityMeasuringService {
	public static ConfigurationKey<Integer> numberOfMeasurementsKey = ConfigurationKey
			.Create(Integer.class,
					"qms.numberOfMeasurements",
					"number of measurements the QuantityMeasuringService should perform to get reliable results",
					10);
	private static Logger log = Logger
			.getLogger(QuantityMeasuringService.class);
	@Inject
	MeasurementService measurementService;

	@Inject
	SystemInfoRepository pmuRepository;

	@Inject
	Configuration configuration;

	public enum Operation {
		SinglePrecisionFlop, DoublePrecisionFlop, CompInstr, SSEFlop,
	}

	public static final Axis<Operation> operationAxis = new Axis<Operation>(
			"0cbbc641-c59d-4027-bd5b-e0144da82227", "operation");

	public enum MemoryTransferBorder {
		CpuL1, L1L2, L2L3, LlcRam,
	}

	public static final Axis<MemoryTransferBorder> memoryTransferBorderAxis = new Axis<MemoryTransferBorder>(
			"ac9c15ce-5a16-43c1-a21e-33fe59047dae", "memoryTransferBorder");

	public enum ClockType {
		CoreCycles, ReferenceCycles, uSecs
	}

	public static final Axis<ClockType> clockTypeAxis = new Axis<ClockType>(
			"fdfe94b9-4690-4c94-8d71-c10e8ede4748", "clockType");

	public static final Axis<Class<?>> quantityAxis = new Axis<Class<?>>(
			"6c426432-5521-4c93-ac65-ec5d51a062bc", "quantity");

	public OperationalIntensity measureOperationalIntensity(KernelBase kernel,
			MemoryTransferBorder border, Operation operation) {
		return new OperationalIntensity(
				measureTransferredBytes(kernel, border), measureOperationCount(
						kernel, operation));
	}

	public Performance measurePerformance(KernelBase kernel,
			Operation operation, ClockType clockType) {
		return new Performance(measureOperationCount(kernel, operation),
				measureExecutionTime(kernel, clockType));
	}

	public Throughput measureThroughput(KernelBase kernel,
			MemoryTransferBorder border, ClockType clockType) {
		return new Throughput(measureTransferredBytes(kernel, border),
				measureExecutionTime(kernel, clockType));
	}

	public OperationCount measureOperationCount(KernelBase kernel,
			Operation operation) {

		return measure(kernel, getOperationCountCalculator(operation));

	}

	private <T extends Quantity<T>> TerminalQuantityCalculator<T> createPerfEventQuantityCalculator(
			final Class<T> clazz, String... events) {
		final PerfEventMeasurer measurer = new PerfEventMeasurer();

		final String eventDefinition = pmuRepository.getAvailableEvent(events);
		measurer.addEvent("count", eventDefinition);

		return new TerminalQuantityCalculator<T>(measurer) {

			@Override
			public T getResult(Iterable<MeasurerOutputBase> outputs) {
				PerfEventCount eventCount = single(outputs).cast(measurer)
						.getEventCount("count");
				log.debug(String.format("eventDef: %s, %s", eventDefinition,
						eventCount));
				return Quantity.construct(clazz, eventCount.getScaledCount());
			}
		};
	}

	public QuantityCalculator<OperationCount> getOperationCountCalculator(
			Operation operation) {

		// handle the allOperations case
		if (operation == Operation.SSEFlop) {
			return new AddingQuantityCalculator<OperationCount>(
					getOperationCountCalculator(Operation.SinglePrecisionFlop),
					getOperationCountCalculator(Operation.DoublePrecisionFlop));
		}

		// perform measurement
		switch (operation) {
		case SSEFlop:
			throw new Error("should not happen");
		case SinglePrecisionFlop: {
			QuantityCalculator<OperationCount> scalar = createPerfEventQuantityCalculator(
					OperationCount.class,
					"coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:SCALAR_SINGLE",
					"core::SIMD_COMP_INST_RETIRED:SCALAR_SINGLE");
			QuantityCalculator<OperationCount> packed = createPerfEventQuantityCalculator(
					OperationCount.class,
					"coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:PACKED_SINGLE",
					"core::SIMD_COMP_INST_RETIRED:PACKED_SINGLE");
			return new AddingQuantityCalculator<OperationCount>(
					scalar,
					new MultiplyingQuantityCalculator<OperationCount>(packed, 2));
		}

		case DoublePrecisionFlop: {
			QuantityCalculator<OperationCount> scalar = createPerfEventQuantityCalculator(
					OperationCount.class,
					"coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:SCALAR_DOUBLE",
					"core::SIMD_COMP_INST_RETIRED:SCALAR_DOUBLE");
			QuantityCalculator<OperationCount> packed = createPerfEventQuantityCalculator(
					OperationCount.class,
					"coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:PACKED_DOUBLE",
					"core::SIMD_COMP_INST_RETIRED:PACKED_DOUBLE");

			if (pmuRepository.getPresentPMU("coreduo") != null) {
				// the PACKED_DOUBLE counter is buggy on the core duo and includes
				// the SCALAR_DOUBLE events as well. Compensate

				return new SubtractingQuantityCalculator<OperationCount>(
						new MultiplyingQuantityCalculator<OperationCount>(
								packed, 2), scalar);
			}
			else {
				return new AddingQuantityCalculator<OperationCount>(scalar,
						new MultiplyingQuantityCalculator<OperationCount>(
								packed, 2));
			}
		}

		case CompInstr:
			return createPerfEventQuantityCalculator(OperationCount.class,
					"coreduo::FP_COMP_INSTR_RET", "core::FP_COMP_OPS_EXE");

		default:
			throw new Error("should not happen");
		}

	}

	public TransferredBytes measureTransferredBytes(KernelBase kernel,
			MemoryTransferBorder border) {
		return measure(kernel, getTransferredBytesCalculator(border));
	}

	public QuantityCalculator<TransferredBytes> getTransferredBytesCalculator(
			MemoryTransferBorder border) {

		switch (border) {
		case CpuL1:
		case L1L2:
		case L2L3:
			throw new Error("Not Supported");
		case LlcRam:
			return new MultiplyingQuantityCalculator<TransferredBytes>(
					createPerfEventQuantityCalculator(TransferredBytes.class,
							"core::BUS_TRANS_MEM:BOTH_CORES",
							"coreduo::BUS_TRANS_MEM"), 64);

		default:
			throw new Error("should not happen");
		}
	}

	public Time measureExecutionTime(KernelBase kernel, ClockType clockType) {
		return measure(kernel, getExecutionTimeCalculator(clockType));
	}

	public QuantityCalculator<Time> getExecutionTimeCalculator(
			ClockType clockType) {

		// setup the measurer
		switch (clockType) {

		case CoreCycles:
			return createPerfEventQuantityCalculator(Time.class,
					"core::UNHALTED_CORE_CYCLES",
					"coreduo::UNHALTED_CORE_CYCLES");
		case ReferenceCycles:
			return createPerfEventQuantityCalculator(Time.class,
					"perf::PERF_COUNT_HW_BUS_CYCLES");
		case uSecs: {
			final ExecutionTimeMeasurer measurer = new ExecutionTimeMeasurer();
			return new TerminalQuantityCalculator<Time>(measurer) {

				@Override
				public Time getResult(Iterable<MeasurerOutputBase> outputs) {
					return new Time(single(outputs).cast(measurer).getUSecs());
				}
			};
		}
		default:
			throw new Error("Should not happen");
		}
	}

	private <T extends Quantity<T>, TCalc extends QuantityCalculator<T>> T measure(
			KernelBase kernel, TCalc calculator) {

		int numMeasurements = configuration.get(numberOfMeasurementsKey);

		// get results for each required measurer set
		ArrayList<MeasurementResult> measurementResults = new ArrayList<MeasurementResult>();
		for (MeasurerBase measurer : calculator.getRequiredMeasurers()) {
			Measurement measurement = new Measurement();
			Workload workload = new Workload();
			workload.setKernel(kernel);
			workload.setMeasurerSet(new MeasurerSet(measurer));
			measurement.addWorkload(workload);

			MeasurementResult result = measurementService.measure(measurement,
					numMeasurements);
			measurementResults.add(result);

		}

		ArrayList<T> results = new ArrayList<T>();
		// calculate the resulting quantity for all corrresponding results
		for (int runNr = 0; runNr < numMeasurements; runNr++) {
			ArrayList<MeasurerOutputBase> outputs = new ArrayList<MeasurerOutputBase>();
			for (int setNr = 0; setNr < calculator.getRequiredMeasurers()
					.size(); setNr++) {
				MeasurerBase measurer = calculator.getRequiredMeasurers().get(
						setNr);
				MeasurementResult result = measurementResults.get(setNr);
				outputs.add(result.getOutputs().get(runNr)
						.getMeasurerOutput(measurer));
			}
			results.add(calculator.getResult(outputs));
		}

		return min(results, Quantity.<T> lessThan());
	}

	public QuantityCalculator<ContextSwitches> getContextSwitchesCalculator() {
		return createPerfEventQuantityCalculator(ContextSwitches.class,
				"perf::PERF_COUNT_SW_CONTEXT_SWITCHES");
	}

	public QuantityCalculator<Interrupts> getInterruptsCalculator() {
		return createPerfEventQuantityCalculator(Interrupts.class,
				"coreduo::HW_INT_RX");
	}

	public Quantity<?> measure(KernelBase kernel, Coordinate measurementPoint) {
		Class<?> quantity = measurementPoint.get(quantityAxis);

		if (quantity == Time.class) {
			return measureExecutionTime(kernel,
					measurementPoint.get(clockTypeAxis));
		}

		if (quantity == Throughput.class) {
			return measureThroughput(kernel,
					measurementPoint.get(memoryTransferBorderAxis),
					measurementPoint.get(clockTypeAxis));
		}

		if (quantity == TransferredBytes.class) {
			return measureTransferredBytes(kernel,
					measurementPoint.get(memoryTransferBorderAxis));
		}

		if (quantity == OperationCount.class) {
			return measureOperationCount(kernel,
					measurementPoint.get(operationAxis));
		}

		if (quantity == Performance.class) {
			return measurePerformance(kernel,
					measurementPoint.get(operationAxis),
					measurementPoint.get(clockTypeAxis));
		}

		throw new Error("should not happen");
	}
}
