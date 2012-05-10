package ch.ethz.ruediste.roofline.measurementDriver.dom.services;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import ch.ethz.ruediste.roofline.measurementDriver.configuration.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.TerminalQuantityCalculator.Combination;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.*;

import com.google.inject.Inject;

public class QuantityMeasuringService {
	public static ConfigurationKey<Integer> numberOfRunsKey = ConfigurationKey
			.Create(Integer.class,
					"qms.numberOfRuns",
					"number of runs the QuantityMeasuringService should perform to get reliable results",
					10);

	private static Logger log = Logger
			.getLogger(QuantityMeasuringService.class);

	@Inject
	public MeasurementService measurementService;

	@Inject
	public SystemInfoService systemInfoService;

	@Inject
	public Configuration configuration;

	public static final Axis<Operation> operationAxis = new Axis<Operation>(
			"0cbbc641-c59d-4027-bd5b-e0144da82227", "operation");

	public enum MemoryTransferBorder {
		CpuL1, L1L2, L2L3, LlcRamBus, LlcRamLines, LlcRamLinesWrite,
	}

	public static final Axis<MemoryTransferBorder> memoryTransferBorderAxis = new Axis<MemoryTransferBorder>(
			"ac9c15ce-5a16-43c1-a21e-33fe59047dae", "memoryTransferBorder");

	public static final Axis<ClockType> clockTypeAxis = new Axis<ClockType>(
			"fdfe94b9-4690-4c94-8d71-c10e8ede4748", "clockType");

	public static final Axis<Class<?>> quantityAxis = new Axis<Class<?>>(
			"6c426432-5521-4c93-ac65-ec5d51a062bc", "quantity");

	public QuantityCalculator<OperationalIntensity> getOperationalIntensityCalculator(
			MemoryTransferBorder border, Operation operation) {
		QuantityCalculator<TransferredBytes> tbCalculator = getTransferredBytesCalculator(border);
		QuantityCalculator<OperationCount> opCountCalculator = getOperationCountCalculator(operation);
		return new DividingQuantityCalculator<OperationalIntensity, OperationCount, TransferredBytes>(
				OperationalIntensity.class, opCountCalculator, tbCalculator);
	}

	public QuantityCalculator<Performance> getPerformanceCalculator(
			Operation operation, ClockType clockType) {
		QuantityCalculator<Time> timeCalc = getTimeCalculator(clockType);
		QuantityCalculator<OperationCount> opCalc = getOperationCountCalculator(operation);
		return DividingQuantityCalculator.create(
				Performance.class,
				opCalc,
				timeCalc);
	}

	public QuantityCalculator<Throughput> getThroughputCalculator(
			MemoryTransferBorder border, ClockType clockType) {
		return new DividingQuantityCalculator<Throughput, TransferredBytes, Time>(
				Throughput.class, getTransferredBytesCalculator(border),
				getTimeCalculator(clockType));

	}

	/**
	 * Creates a quantity calculator for an event. Multiple event definitions
	 * can be passed. The single available event is measured.
	 */
	private <T extends Quantity<T>> TerminalQuantityCalculator<T> createPerfEventQuantityCalculator(
			final Class<T> clazz, final Combination combination,
			String... events) {
		final PerfEventMeasurer measurer = new PerfEventMeasurer();

		final String eventDefinition = systemInfoService
				.getAvailableEvent(events);
		measurer.addEvent("count", eventDefinition);

		return new TerminalQuantityCalculator<T>(measurer) {
			{
				setCombination(combination);
			}

			@Override
			public T getSingleResult(Iterable<MeasurerOutputBase> outputs) {
				DescriptiveStatistics stats = new DescriptiveStatistics();
				for (MeasurerOutputBase output : outputs) {
					stats.addValue(output.cast(measurer).getEventCount("count")
							.getScaledCount());
				}
				if (stats.getN() == 0) {
					throw new Error("no outputs");
				}
				return Quantity.construct(clazz,
						getValueRespectingCombination(stats));
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
					Combination.Sum,
					"coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:SCALAR_SINGLE",
					"core::SIMD_COMP_INST_RETIRED:SCALAR_SINGLE",
					"snb_ep::FP_COMP_OPS_EXE:SSE_FP_SCALAR_SINGLE");
			QuantityCalculator<OperationCount> packed = createPerfEventQuantityCalculator(
					OperationCount.class,
					Combination.Sum,
					"coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:PACKED_SINGLE",
					"core::SIMD_COMP_INST_RETIRED:PACKED_SINGLE",
					"snb_ep::FP_COMP_OPS_EXE:SSE_PACKED_SINGLE");
			return new AddingQuantityCalculator<OperationCount>(
					scalar,
					new MultiplyingQuantityCalculator<OperationCount>(packed, 2));
		}

		case DoublePrecisionFlop: {
			QuantityCalculator<OperationCount> scalar = createPerfEventQuantityCalculator(
					OperationCount.class,
					Combination.Sum,
					"coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:SCALAR_DOUBLE",
					"core::SIMD_COMP_INST_RETIRED:SCALAR_DOUBLE",
					"snb_ep::FP_COMP_OPS_EXE:SSE_SCALAR_DOUBLE");
			QuantityCalculator<OperationCount> packed = createPerfEventQuantityCalculator(
					OperationCount.class,
					Combination.Sum,
					"snb_ep::FP_COMP_OPS_EXE:SSE_FP_PACKED_DOUBLE",
					"coreduo::SSE_COMP_INSTRUCTIONS_RETIRED:PACKED_DOUBLE",
					"core::SIMD_COMP_INST_RETIRED:PACKED_DOUBLE");
			
			if (systemInfoService.getCpuType() == CpuType.SandyBridgeExtreme) {
				QuantityCalculator<OperationCount> avx = createPerfEventQuantityCalculator(
						OperationCount.class,
						Combination.Sum,
						"snb_ep::SIMD_FP_256:PACKED_DOUBLE");
				
				
				return 
						new AddingQuantityCalculator<OperationCount>(
								new MultiplyingQuantityCalculator<OperationCount>(avx, 4),
								new AddingQuantityCalculator<OperationCount>(
										scalar,
										new MultiplyingQuantityCalculator<OperationCount>(packed, 2)
										)
								);
								
						

				
				
			}
			else			
			if (systemInfoService.getCpuType() == CpuType.Yonah) {
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
					Combination.Sum, "coreduo::FP_COMP_INSTR_RET",
					"core::FP_COMP_OPS_EXE", //"snb_ep::FP_COMP_OPS_EXE:X87:SSE_FP_PACKED_DOUBLE:SSE_FP_SCALAR_SINGLE:SSE_PACKED_SINGLE:SSE_SCALAR_DOUBLE");
					"snb_ep::INST_RETIRED");

		default:
			throw new Error("should not happen");
		}

	}

	/**
	 * creates a calculator for measuring transferred bytes
	 */
	public QuantityCalculator<TransferredBytes> getTransferredBytesCalculator(
			MemoryTransferBorder border) {

		switch (border) {
		case CpuL1:
		case L1L2:
		case L2L3:
			throw new Error("Not Supported");
		case LlcRamLinesWrite:
			TerminalQuantityCalculator<TransferredBytes> linesOutCalc = createPerfEventQuantityCalculator(
					TransferredBytes.class,
					Combination.Sum,
					"coreduo::L2_M_LINES_OUT:SELF",
					"snb_ep::LLC_MISSES",
					"core::L2_M_LINES_OUT:SELF");
					//snb_ep::LLC_MISSES
/*
			TerminalQuantityCalculator<TransferredBytes> streamingStoreCalc = createPerfEventQuantityCalculator(
					TransferredBytes.class,
					Combination.Sum,
					"coreduo::SSE_NTSTORES_RET", "core::SSE_PRE_EXEC:STORES", "snb_ep::OFFCORE_RESPONSE_0:UMASK ANY_REQUEST");
*/
			return new MultiplyingQuantityCalculator<TransferredBytes>(
					linesOutCalc, 64);
			/*return new AddingQuantityCalculator<TransferredBytes>(
					new MultiplyingQuantityCalculator<TransferredBytes>(
							linesOutCalc, 64), // lines out, multiplied with line length
							
					new MultiplyingQuantityCalculator<TransferredBytes>(
							streamingStoreCalc, 16)); // add streaming stores multiplied with 16 (two doubles)
				*/			
/*
			return new AddingQuantityCalculator<TransferredBytes>(
					new MultiplyingQuantityCalculator<TransferredBytes>(
							linesOutCalc, 64)); // lines out, multiplied with line length 
	*/				
		case LlcRamLines:
			TerminalQuantityCalculator<TransferredBytes> linesInCalc = createPerfEventQuantityCalculator(
					TransferredBytes.class,
					Combination.Sum,
					"coreduo::L2_LINES_IN:SELF",
					"snb_ep::LLC_MISSES",
					"core::L2_LINES_IN:SELF");

			return new AddingQuantityCalculator<TransferredBytes>(
					new MultiplyingQuantityCalculator<TransferredBytes>(
							linesInCalc, 64), // lines in, multiplied with line length
					getTransferredBytesCalculator(MemoryTransferBorder.LlcRamLinesWrite));

		case LlcRamBus:
			return new MultiplyingQuantityCalculator<TransferredBytes>(
					createPerfEventQuantityCalculator(
							TransferredBytes.class, Combination.Sum,
							"core::BUS_TRANS_MEM", "coreduo::BUS_TRANS_MEM", "snb_ep::MEM_UOP_RETIRED:ANY_LOADS"),
					64);

		default:
			throw new Error("should not happen");
		}
	}

	public QuantityCalculator<Time> getTimeCalculator(
			ClockType clockType) {

		// setup the measurer
		switch (clockType) {

		case CoreCycles:
			return createPerfEventQuantityCalculator(Time.class,
					Combination.Max,
					"core::UNHALTED_CORE_CYCLES",
					"snb_ep::UNHALTED_CORE_CYCLES",
					"coreduo::UNHALTED_CORE_CYCLES");
					
		case ReferenceCycles:
			return createPerfEventQuantityCalculator(Time.class,
					Combination.Max, "perf::PERF_COUNT_HW_BUS_CYCLES");
		case uSecs: {
			final ExecutionTimeMeasurer measurer = new ExecutionTimeMeasurer();
			return new TerminalQuantityCalculator<Time>(measurer) {
				{
					setCombination(Combination.Max);
				}

				@Override
				public Time getSingleResult(Iterable<MeasurerOutputBase> outputs) {
					DescriptiveStatistics stats = new DescriptiveStatistics();
					for (MeasurerOutputBase output : outputs) {
						stats.addValue(output.cast(measurer).getUSecs());
					}

					if (stats.getN() == 0) {
						throw new Error("no outputs");
					}

					return new Time(getValueRespectingCombination(stats));
				}
			};
		}
		case TSC: {
			final TscMeasurer measurer = new TscMeasurer();
			return new TerminalQuantityCalculator<Time>(measurer) {
				{
					setCombination(Combination.Max);
				}

				@Override
				public Time getSingleResult(Iterable<MeasurerOutputBase> outputs) {
					DescriptiveStatistics stats = new DescriptiveStatistics();
					for (MeasurerOutputBase output : outputs) {
						stats.addValue(output.cast(measurer).getTics()
								.doubleValue());
					}

					if (stats.getN() == 0) {
						throw new Error("no outputs");
					}

					return new Time(getValueRespectingCombination(stats));
				}
			};
		}
		default:
			throw new Error("Should not happen");
		}
	}

	public QuantityCalculator<ContextSwitches> getContextSwitchesCalculator() {
		return createPerfEventQuantityCalculator(ContextSwitches.class,
				Combination.Sum, "perf::PERF_COUNT_SW_CONTEXT_SWITCHES");
	}

	public QuantityCalculator<Interrupts> getInterruptsCalculator() {
		return createPerfEventQuantityCalculator(Interrupts.class,
				Combination.Sum, "coreduo::HW_INT_RX", "core::HW_INT_RCV");
	}

	public QuantityCalculator<TLBMisses> getTLBMissesCalculator() {
		return createPerfEventQuantityCalculator(
				TLBMisses.class,
				Combination.Sum,
				"coreduo::DTLB_MISS", "core::DTLB_MISSES");
	}

	public Quantity<?> measure(KernelBase kernel, Coordinate measurementPoint) {
		Class<?> quantity = measurementPoint.get(quantityAxis);

		if (quantity == Time.class) {
			QuantityCalculator<Time> calculator = getTimeCalculator(measurementPoint
					.get(clockTypeAxis));
			QuantityMap result = measureQuantities(kernel, calculator);

			return result.best(calculator);
		}

		if (quantity == Throughput.class) {
			QuantityCalculator<Throughput> calc = getThroughputCalculator(
					measurementPoint.get(memoryTransferBorderAxis),
					measurementPoint.get(clockTypeAxis));

			QuantityMap result = measureQuantities(kernel, calc);

			return result.best(calc);
		}

		if (quantity == TransferredBytes.class) {
			QuantityCalculator<TransferredBytes> calculator = getTransferredBytesCalculator(measurementPoint
					.get(memoryTransferBorderAxis));
			QuantityMap result = measureQuantities(kernel, calculator);

			return result.best(calculator);
		}

		if (quantity == OperationCount.class) {
			QuantityCalculator<OperationCount> calculator = getOperationCountCalculator(measurementPoint
					.get(operationAxis));
			QuantityMap result = measureQuantities(kernel, calculator);

			return result.best(calculator);
		}

		if (quantity == Performance.class) {
			QuantityCalculator<Performance> calc = getPerformanceCalculator(
					measurementPoint.get(operationAxis),
					measurementPoint.get(clockTypeAxis));
			QuantityMap result = measureQuantities(kernel, calc);
			return result.best(calc);
		}

		throw new Error("should not happen");
	}

	/**
	 * Result of measuring multiple quantities a single time
	 */
	public class RunQuantityMap extends
			HashMap<QuantityCalculator<?>, Iterable<MeasurerOutputBase>> {
		private static final long serialVersionUID = 1L;

		public <T extends Quantity<T>> T get(QuantityCalculator<T> calc) {
			return calc.getSingleResult(super.get(calc));
		}
	}

	/**
	 * Result of measuring multiple quantities with multiple runs
	 */
	public class QuantityMap extends
			HashMap<QuantityCalculator<?>, List<Iterable<MeasurerOutputBase>>> {
		private static final long serialVersionUID = 2631443018184961723L;

		public <T extends Quantity<T>> List<T> get(QuantityCalculator<T> calc) {
			return calc.getResult(super.get(calc));
		}

		public <T extends Quantity<T>> DescriptiveStatistics getStatistics(
				QuantityCalculator<T> calc) {
			return calc.getStatistics(super.get(calc));
		}

		public <T extends Quantity<T>> T best(QuantityCalculator<T> calc) {
			return calc.getBestResult(super.get(calc));
		}

		/**
		 * group the measurement runs into groups and return the result for each
		 * group
		 * 
		 * @param groupSize
		 *            group size
		 * @return
		 */
		public List<QuantityMap> grouped(int groupSize) {
			ArrayList<QuantityMap> result = new ArrayList<QuantityMap>();

			boolean found = true;
			int idx = 0;

			QuantityMap map = new QuantityMap();
			while (found) {
				found = false;

				// iterate over all entries
				for (java.util.Map.Entry<QuantityCalculator<?>, List<Iterable<MeasurerOutputBase>>> entry : entrySet()) {
					// is there a value for the current index?
					if (idx < entry.getValue().size()) {
						found = true;

						// get the list for the calculator
						List<Iterable<MeasurerOutputBase>> list = map
								.get((Object) entry.getKey());
						if (list == null) {
							list = new ArrayList<Iterable<MeasurerOutputBase>>();
							map.put(entry.getKey(), list);
						}

						// add the run output to the list of the calculator
						list.add(entry.getValue().get(idx));
					}
				}

				if (found && idx > 0 && idx % groupSize == 0) {
					result.add(map);
					map = new QuantityMap();
				}

				idx++;
			}

			// add a partial map if persent
			if (map.size() > 0)
				result.add(map);

			return result;
		}

		/**
		 * return a RunQuantityMap for each run
		 */
		public List<RunQuantityMap> getRunMaps() {
			ArrayList<RunQuantityMap> result = new ArrayList<QuantityMeasuringService.RunQuantityMap>();

			boolean found = true;
			int idx = 0;

			while (found) {
				found = false;
				RunQuantityMap map = new RunQuantityMap();
				// iterate over all entries
				for (java.util.Map.Entry<QuantityCalculator<?>, List<Iterable<MeasurerOutputBase>>> entry : entrySet()) {
					// is there a value for the current index?
					if (idx < entry.getValue().size()) {
						found = true;
						map.put(entry.getKey(), entry.getValue().get(idx));
					}
				}

				if (found) {
					result.add(map);
				}

				idx++;
			}

			return result;
		}

	}

	public interface IMeasurementBuilder {
		Measurement build(Map<Object, MeasurerSet> sets);
	}

	public class ArgBuilderGetQuantities {
		private final ArrayList<Pair<Object, QuantityCalculator<?>[]>> args = new ArrayList<Pair<Object, QuantityCalculator<?>[]>>();
		IMeasurementBuilder measurementBuilder;
		private final int numberOfMeasurements;

		public ArgBuilderGetQuantities(IMeasurementBuilder measurementBuilder,
				int numberOfMeasurements) {
			super();
			this.measurementBuilder = measurementBuilder;
			this.numberOfMeasurements = numberOfMeasurements;
		}

		public ArgBuilderGetQuantities with(Object key,
				QuantityCalculator<?>... calculators) {
			args.add(Pair.of(key, calculators));
			return this;
		}

		public QuantityMap get() {
			return mesaureQuantities(measurementBuilder, numberOfMeasurements,
					args);
		}
	}

	public ArgBuilderGetQuantities measureQuantities(
			IMeasurementBuilder measurementBuilder) {
		return measureQuantities(measurementBuilder,
				configuration.get(numberOfRunsKey));

	}

	public ArgBuilderGetQuantities measureQuantities(
			IMeasurementBuilder measurementBuilder, int numberOfRuns) {
		return new ArgBuilderGetQuantities(measurementBuilder,
				numberOfRuns);
	}

	public QuantityMap mesaureQuantities(
			IMeasurementBuilder measurementBuilder, int numberOfRuns,
			List<Pair<Object, QuantityCalculator<?>[]>> calculatorGroups) {

		// extract the measurer sets for the calculators of each group
		ArrayList<Pair<Object, List<MeasurerSet>>> measurerSets = new ArrayList<Pair<Object, List<MeasurerSet>>>();
		for (Pair<Object, QuantityCalculator<?>[]> entry : calculatorGroups) {
			List<MeasurerBase> measurers = new ArrayList<MeasurerBase>();
			for (QuantityCalculator<?> calc : entry.getRight()) {
				measurers.addAll(calc.getRequiredMeasurers());
			}

			Pair<Object, List<MeasurerSet>> pair = Pair.of(entry.getLeft(),
					measurementService.buildSets(measurers));

			measurerSets.add(pair);
		}

		ArrayList<Map<Object, MeasurerSet>> setsForMeasurements = new ArrayList<Map<Object, MeasurerSet>>();
		// group the measurer sets of the groups to groups of measurer sets which should 
		// be measured in one measurement
		{
			boolean anyAdded;
			int idx = 0;
			do {
				anyAdded = false;
				Map<Object, MeasurerSet> map = new HashMap<Object, MeasurerSet>();
				for (Pair<Object, List<MeasurerSet>> pair : measurerSets) {
					if (pair.getValue().size() > idx) {
						anyAdded = true;
						map.put(pair.getKey(), pair.getValue().get(idx));
					}
				}
				if (anyAdded) {
					setsForMeasurements.add(map);
				}
				idx++;
			}
			while (anyAdded);
		}

		ArrayList<MeasurementResult> results = new ArrayList<MeasurementResult>();

		// iterate over the required measurements and perform the measurements
		for (Map<Object, MeasurerSet> setsForMeasurement : setsForMeasurements) {
			// get the measurement
			Measurement measurement = measurementBuilder
					.build(setsForMeasurement);

			// run the measurement
			MeasurementResult result = measurementService.measure(measurement,
					numberOfRuns);

			// add the result to the results list
			results.add(result);
		}

		// combine the results of each run from the different measurements
		ArrayList<List<MeasurerOutputBase>> combinedResults = new ArrayList<List<MeasurerOutputBase>>();
		{
			// iterate over the runs
			for (int idx = 0; idx < numberOfRuns; idx++) {
				// for each run, combine the outputs of all measurements
				ArrayList<MeasurerOutputBase> tmp = new ArrayList<MeasurerOutputBase>();
				for (MeasurementResult result : results) {
					Iterable<MeasurerOutputBase> measurerOutputsOfOneRun = result
							.getRunOutputs().get(idx)
							.getMeasurerOutputs();

					addAll(tmp, measurerOutputsOfOneRun);
				}
				combinedResults.add(tmp);
			}
		}

		QuantityMap resultMap = new QuantityMap();
		// calculate the quantities for each quantity calculator
		for (Pair<Object, QuantityCalculator<?>[]> pair : calculatorGroups) {
			for (QuantityCalculator<?> calculator : pair.getRight()) {
				// will contain a quantity for each run 
				ArrayList<Iterable<MeasurerOutputBase>> filteredRunOutputs = new ArrayList<Iterable<MeasurerOutputBase>>();

				// iterate over the combined results of each measurement run
				for (List<MeasurerOutputBase> measurerOutputs : combinedResults) {
					// calculate the quantity for each run

					filteredRunOutputs.add(where(measurerOutputs,
							calculator.isFromRequiredMeasurer()));
				}

				resultMap.put(calculator, filteredRunOutputs);
			}
		}

		return resultMap;
	}

	/**
	 * @param kernel
	 * @param calculator
	 * @return
	 */
	public QuantityMap measureQuantities(final KernelBase kernel,
			QuantityCalculator<?>... calculators) {
		int numMeasurements = configuration.get(numberOfRunsKey);

		QuantityMap result = measureQuantities(kernel, numMeasurements,
				calculators);
		return result;
	}

	/**
	 * Measure a single workload with the given kernel for all measurers
	 * required by the calculators
	 */
	public QuantityMap measureQuantities(final KernelBase kernel, int numRuns,
			QuantityCalculator<?>... calculators) {
		IMeasurementBuilder builder = new IMeasurementBuilder() {

			public Measurement build(Map<Object, MeasurerSet> sets) {
				Measurement measurement = new Measurement();
				Workload workload = new Workload();
				workload.setKernel(kernel);
				workload.setMeasurerSet(sets.get("main"));
				measurement.addWorkload(workload);
				return measurement;
			}
		};
		QuantityMap result = measureQuantities(builder, numRuns).with("main",
				calculators).get();
		return result;
	}
}
