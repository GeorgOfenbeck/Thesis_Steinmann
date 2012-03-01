package ch.ethz.ruediste.roofline.measurementDriver.services;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.SystemInfoRepository;
import ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils;
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

	public static final Axis<Operation> operationAxis = new Axis<Operation>(
			"0cbbc641-c59d-4027-bd5b-e0144da82227", "operation");

	public enum MemoryTransferBorder {
		CpuL1, L1L2, L2L3, LlcRam,
	}

	public static final Axis<MemoryTransferBorder> memoryTransferBorderAxis = new Axis<MemoryTransferBorder>(
			"ac9c15ce-5a16-43c1-a21e-33fe59047dae", "memoryTransferBorder");

	public static final Axis<ClockType> clockTypeAxis = new Axis<ClockType>(
			"fdfe94b9-4690-4c94-8d71-c10e8ede4748", "clockType");

	public static final Axis<Class<?>> quantityAxis = new Axis<Class<?>>(
			"6c426432-5521-4c93-ac65-ec5d51a062bc", "quantity");

	public OperationalIntensity measureOperationalIntensity(KernelBase kernel,
			MemoryTransferBorder border, Operation operation) {
		QuantityCalculator<TransferredBytes> tbCalculator = getTransferredBytesCalculator(border);
		QuantityCalculator<OperationCount> opCountCalculator = getOperationCountCalculator(operation);

		QuantityMap result = measureQuantities(kernel, opCountCalculator,
				tbCalculator);

		return new OperationalIntensity(result.min(tbCalculator),
				result.min(opCountCalculator));
	}

	public Performance measurePerformance(KernelBase kernel,
			Operation operation, ClockType clockType) {
		QuantityCalculator<OperationCount> opCalc = getOperationCountCalculator(operation);
		QuantityCalculator<Time> timeCalc = getExecutionTimeCalculator(clockType);
		QuantityMap result = measureQuantities(kernel, timeCalc, opCalc);
		return new Performance(result.min(opCalc), result.min(timeCalc));
	}

	public Throughput measureThroughput(KernelBase kernel,
			MemoryTransferBorder border, ClockType clockType) {

		QuantityCalculator<TransferredBytes> tbCalc = getTransferredBytesCalculator(border);
		QuantityCalculator<Time> timeCalc = getExecutionTimeCalculator(clockType);

		QuantityMap result = measureQuantities(kernel, tbCalc, timeCalc);

		return new Throughput(result.min(tbCalc), result.min(timeCalc));
	}

	public OperationCount measureOperationCount(KernelBase kernel,
			Operation operation) {

		QuantityCalculator<OperationCount> calculator = getOperationCountCalculator(operation);
		QuantityMap result = measureQuantities(kernel, calculator);

		return result.min(calculator);

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
		QuantityCalculator<TransferredBytes> calculator = getTransferredBytesCalculator(border);
		QuantityMap result = measureQuantities(kernel, calculator);

		return result.min(calculator);
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
		QuantityCalculator<Time> calculator = getExecutionTimeCalculator(clockType);
		QuantityMap result = measureQuantities(kernel, calculator);

		return result.min(calculator);
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

	/**
	 * @param kernel
	 * @param calculator
	 * @return
	 */
	public QuantityMap measureQuantities(final KernelBase kernel,
			QuantityCalculator<?>... calculators) {
		int numMeasurements = configuration.get(numberOfMeasurementsKey);

		IMeasurementBuilder builder = new IMeasurementBuilder() {

			public Measurement build(Map<String, MeasurerSet> sets) {
				Measurement measurement = new Measurement();
				Workload workload = new Workload();
				workload.setKernel(kernel);
				workload.setMeasurerSet(sets.get("main"));
				measurement.addWorkload(workload);
				return measurement;
			}
		};
		QuantityMap result = getQuantities(builder, numMeasurements).with(
				"main", calculators).get();
		return result;
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

	public class QuantityMap extends
			HashMap<QuantityCalculator<?>, List<Quantity<?>>> {
		private static final long serialVersionUID = 2631443018184961723L;

		@SuppressWarnings("unchecked")
		public <T extends Quantity<T>> List<T> get(QuantityCalculator<T> calc) {
			ArrayList<T> result = new ArrayList<T>();
			for (Quantity<?> q : super.get(calc)) {
				result.add((T) q);
			}

			return Collections.unmodifiableList(result);
		}

		public <T extends Quantity<T>> T min(QuantityCalculator<T> calc) {
			return IterableUtils.min(get(calc), Quantity.<T> lessThan());
		}

	}

	public interface IMeasurementBuilder {
		Measurement build(Map<String, MeasurerSet> sets);
	}

	public class ArgBuilderGetQuantities {
		private ArrayList<Pair<String, QuantityCalculator<?>[]>> args = new ArrayList<Pair<String, QuantityCalculator<?>[]>>();
		IMeasurementBuilder measurementBuilder;
		private final int numberOfMeasurements;

		public ArgBuilderGetQuantities(IMeasurementBuilder measurementBuilder,
				int numberOfMeasurements) {
			super();
			this.measurementBuilder = measurementBuilder;
			this.numberOfMeasurements = numberOfMeasurements;
		}

		public ArgBuilderGetQuantities with(String name,
				QuantityCalculator<?>... calculators) {
			args.add(Pair.of(name, calculators));
			return this;
		}

		public QuantityMap get() {
			return getQuantities(measurementBuilder, numberOfMeasurements, args);
		}
	}

	public ArgBuilderGetQuantities getQuantities(
			IMeasurementBuilder measurementBuilder, int numberOfMeasurements) {
		return new ArgBuilderGetQuantities(measurementBuilder,
				numberOfMeasurements);
	}

	public QuantityMap getQuantities(IMeasurementBuilder measurementBuilder,
			int numberOfMeasurements,
			List<Pair<String, QuantityCalculator<?>[]>> calculators) {

		// extract the measurer sets for the calculators of each group
		ArrayList<Pair<String, List<MeasurerSet>>> measurerSets = new ArrayList<Pair<String, List<MeasurerSet>>>();
		for (Pair<String, QuantityCalculator<?>[]> entry : calculators) {
			List<MeasurerBase> measurers = new ArrayList<MeasurerBase>();
			for (QuantityCalculator<?> calc : entry.getRight()) {
				measurers.addAll(calc.getRequiredMeasurers());
			}

			measurerSets.add(Pair.of(entry.getLeft(),
					measurementService.buildSets(measurers)));
		}

		ArrayList<Map<String, MeasurerSet>> setsForMeasurements = new ArrayList<Map<String, MeasurerSet>>();
		// group the measurer sets of the groups to groups of measurer sets which should 
		// be measured in one measurement
		{
			boolean anyAdded;
			int idx = 0;
			do {
				anyAdded = false;
				Map<String, MeasurerSet> map = new HashMap<String, MeasurerSet>();
				for (Pair<String, List<MeasurerSet>> pair : measurerSets) {
					if (pair.getValue().size() > idx) {
						anyAdded = true;
						map.put(pair.getKey(), pair.getValue().get(idx));
					}
				}
				if (anyAdded)
					setsForMeasurements.add(map);
				idx++;
			}
			while (anyAdded);
		}

		ArrayList<MeasurementResult> results = new ArrayList<MeasurementResult>();

		// iterate over the required measurements and perform the measurements
		for (Map<String, MeasurerSet> setsForMeasurement : setsForMeasurements) {
			// get the measurement
			Measurement measurement = measurementBuilder
					.build(setsForMeasurement);

			// run the measurement
			MeasurementResult result = measurementService.measure(measurement,
					numberOfMeasurements);

			// add the result to the results list
			results.add(result);
		}

		// combine the results of each run from the different measurements
		ArrayList<List<MeasurerOutputBase>> combinedResults = new ArrayList<List<MeasurerOutputBase>>();
		{
			for (int idx = 0; idx < numberOfMeasurements; idx++) {
				ArrayList<MeasurerOutputBase> tmp = new ArrayList<MeasurerOutputBase>();
				for (MeasurementResult result : results) {
					addAll(tmp, result.getRunOutputs().get(idx)
							.getMeasurerOutputs());
				}
				combinedResults.add(tmp);
			}
		}

		QuantityMap resultMap = new QuantityMap();

		// calculate the quantities for each quantity calculator
		for (Pair<String, QuantityCalculator<?>[]> pair : calculators) {
			for (QuantityCalculator<?> calculator : pair.getRight()) {
				ArrayList<Quantity<?>> quantities = new ArrayList<Quantity<?>>();
				for (List<MeasurerOutputBase> measurerOutputs : combinedResults) {
					quantities.add(calculator.getResult(where(measurerOutputs,
							calculator.isFromRequiredMeasurer())));
				}
				resultMap.put(calculator, quantities);
			}
		}

		return resultMap;
	}
}
