package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.entities.Axes.clockTypeAxis;
import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;

import java.io.IOException;
import java.util.*;

import ch.ethz.ruediste.roofline.entities.MeasurementResult;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.SystemInfoRepository;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.actions.*;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.ArithmeticKernel.ArithmeticOperation;

import com.google.inject.Inject;

public class ExpTime1MeasurementController implements IMeasurementController {

	public String getName() {
		return "exptime1";
	}

	public String getDescription() {
		return "timin experimen t1";
	}

	@Inject
	MeasurementService measurementService;

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	@Inject
	SystemInfoRepository systemInfoRepository;

	private enum SystemLoad {
		Idle, DiskOther, DiskAll, AddOther, AddAll,
	}

	private static Axis<SystemLoad> systemLoadAxis = new Axis<SystemLoad>(
			"dea44497-3db7-4037-8eda-ff1136e158d0", "workload");

	public void measure(String outputName) throws IOException {
		ParameterSpace space = new ParameterSpace();

		// get reference iterations
		long referenceIterations = getReferenceIterations();
		System.out.printf("Iteration count without context switches: %d\n",
				referenceIterations);

		{
			Time executionTime = measureTime(referenceIterations,
					ClockType.uSecs, SystemLoad.Idle);

			System.out.printf("Execution time for reference iterations: %s\n",
					executionTime);
		}

		// setup the idle system workload
		space.add(systemLoadAxis, SystemLoad.Idle);

		// setup workload with disk IO kernels on all but the first core
		space.add(systemLoadAxis, SystemLoad.DiskOther);

		// setup workload with disk IO kernels on all cores
		space.add(systemLoadAxis, SystemLoad.DiskAll);

		// setup workload with add kernels on all but the first core
		space.add(systemLoadAxis, SystemLoad.AddOther);

		// setup workload with add kernels on all cores
		space.add(systemLoadAxis, SystemLoad.AddAll);

		space.add(clockTypeAxis, ClockType.CoreCycles);
		space.add(clockTypeAxis, ClockType.ReferenceCycles);
		space.add(clockTypeAxis, ClockType.uSecs);

		for (Coordinate coordinate : space) {
			boolean enoughIterations = false;
			for (long iterations = 1; !enoughIterations; iterations *= 2) {

				ClockType clockType = coordinate.get(clockTypeAxis);
				SystemLoad systemLoad = coordinate.get(systemLoadAxis);

				Time executionTime = measureTime(iterations, clockType,
						systemLoad);

				System.out.printf("%s: Iterations: %d Execution time: %s\n",
						coordinate, iterations, executionTime);

				// check if there are enough iterations already
				switch (clockType) {
				case CoreCycles:
					enoughIterations = executionTime.getValue() > 1e9;
				break;
				case ReferenceCycles:
					enoughIterations = executionTime.getValue() > 100e6;
				break;
				case uSecs:
					enoughIterations = executionTime.getValue() > 500000.0;
				break;

				}
			}
		}

	}

	/**
	 * @param iterations
	 * @param clockType
	 * @param systemLoad
	 * @return
	 */
	public Time measureTime(long iterations, ClockType clockType,
			SystemLoad systemLoad) {
		QuantityCalculator<Time> timeCalc = quantityMeasuringService
				.getExecutionTimeCalculator(clockType);

		Workload mainWorkload = new Workload();

		Measurement measurement = createMeasurement(systemLoad, iterations,
				timeCalc, mainWorkload);

		// perform measurement
		MeasurementResult result = measurementService.measure(measurement, 1);

		Time executionTime = timeCalc.getResult(result
				.getMeasurerOutputsUntyped(single(timeCalc
						.getRequiredMeasurers())));
		return executionTime;
	}

	private long getReferenceIterations() {

		QuantityCalculator<Interrupts> interruptsCalc = quantityMeasuringService
				.getInterruptsCalculator();

		Workload mainWorkload = new Workload();
		long lastIterations = 0;

		for (long iterations = 1000;; iterations *= 1.2) {
			Measurement measurement = createMeasurement(SystemLoad.Idle,
					iterations, interruptsCalc, mainWorkload);

			// perform measurement
			MeasurementResult result = measurementService.measure(measurement,
					10);

			Iterable<MeasurerOutputBase> outputs = result
					.getMeasurerOutputsUntyped(single(interruptsCalc
							.getRequiredMeasurers()));
			// take minimum
			Interrupts interrupts = min(
					// turn outputs into quantities
					select(outputs, Quantity.resultToQuantity(interruptsCalc)),
					Quantity.<Interrupts> lessThan());

			// check if there were any context switches
			if (interrupts.getValue() > 0.1) {
				// return the iteration count without context switches
				return lastIterations;
			}
			lastIterations = iterations;
		}
	}

	/**
	 * @param coordinate
	 * @param iterations
	 * @param quantityCalc
	 * @param mainWorkload
	 * @return
	 */
	public Measurement createMeasurement(SystemLoad systemLoad,
			long iterations, QuantityCalculator<?> quantityCalc,
			Workload mainWorkload) {
		Measurement measurement = new Measurement();
		ArrayList<Workload> systemLoadWorkloads = createWorkloads(systemLoad);

		// add system load workloads to measurement
		for (Workload loadWorkload : systemLoadWorkloads) {
			measurement.addWorkload(loadWorkload);
		}

		// setup main workload

		measurement.addWorkload(mainWorkload);

		ArithmeticKernel arithmeticKernel = new ArithmeticKernel();
		arithmeticKernel.setOptimization("-O3");
		arithmeticKernel.setIterations(iterations);
		arithmeticKernel
				.setOperation(ArithmeticOperation.ArithmeticOperation_ADD);

		mainWorkload.setKernel(arithmeticKernel);

		mainWorkload.setMeasurerSet(new MeasurerSet(single(quantityCalc
				.getRequiredMeasurers())));

		// setup stop rules for system load workloads
		for (Workload loadWorkload : systemLoadWorkloads) {
			Rule stopRule = new Rule();
			stopRule.setPredicate(new WorkloadStopEventPredicate(mainWorkload));
			stopRule.setAction(new StopKernelAction(loadWorkload.getKernel()));
			measurement.addRule(stopRule);
		}

		// setup rule for waiting for system load workloads
		for (Workload loadWorkload : systemLoadWorkloads) {
			Rule startRule = new Rule();
			startRule
					.setPredicate(new WorkloadStartEventPredicate(mainWorkload));
			startRule.setAction(new WaitForWorkloadAction(loadWorkload));
			measurement.addRule(startRule);
		}
		return measurement;
	}

	public ArrayList<Workload> createWorkloads(SystemLoad workload) {
		List<Integer> cpus = systemInfoRepository.getOnlineCPUs();
		switch (workload) {
		case AddAll:
			return createAddWorkloads(cpus);
		case AddOther:
			return createAddWorkloads(tail(cpus));
		case DiskAll:
			return createDiskIoWorkloads(cpus);
		case DiskOther:
			return createDiskIoWorkloads(tail(cpus));
		case Idle:
			return new ArrayList<Workload>();
		}
		throw new Error("should not happen");
	}

	public ArrayList<Workload> createDiskIoWorkloads(Iterable<Integer> cpus) {
		ArrayList<Workload> result = new ArrayList<Workload>();
		for (Integer cpu : cpus) {
			Workload diskIoWorkload = new Workload();
			diskIoWorkload.setCpu(cpu);

			DiskIoKernel diskIoKernel = new DiskIoKernel();
			diskIoKernel.setFileSize(1024 * 1024 * 2L);
			diskIoKernel.setIterations(-1);
			diskIoWorkload.setKernel(diskIoKernel);
			result.add(diskIoWorkload);
		}
		return result;
	}

	public ArrayList<Workload> createAddWorkloads(Iterable<Integer> cpus) {
		ArrayList<Workload> result = new ArrayList<Workload>();
		for (Integer cpu : cpus) {
			Workload workload = new Workload();
			workload.setCpu(cpu);

			ArithmeticKernel kernel = new ArithmeticKernel();
			kernel.setOperation(ArithmeticOperation.ArithmeticOperation_ADD);
			kernel.setOptimization("-O3");
			kernel.setRunUntilStopped(true);
			workload.setKernel(kernel);
			result.add(workload);
		}
		return result;
	}
}
