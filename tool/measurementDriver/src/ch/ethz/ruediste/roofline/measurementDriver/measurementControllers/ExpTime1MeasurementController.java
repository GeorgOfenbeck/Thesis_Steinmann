package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.dom.Axes.clockTypeAxis;
import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;

import java.io.IOException;
import java.util.ArrayList;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.ArithmeticKernel.ArithmeticOperation;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Time;
import ch.ethz.ruediste.roofline.measurementDriver.repositories.SystemInfoRepository;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;
import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryFunction;

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

	public void measure(String outputName) throws IOException {
		ParameterSpace space = new ParameterSpace();

		// setup workloads
		Axis<ArrayList<Workload>> systemLoadAxis = new Axis<ArrayList<Workload>>(
				"dea44497-3db7-4037-8eda-ff1136e158d0", "workload", null,
				new IUnaryFunction<ArrayList<Workload>, String>() {

					public String apply(ArrayList<Workload> arg) {
						if (arg.size() == 0) {
							return "idle";
						}
						return first(arg).getKernel().getClass()
								.getSimpleName()
								+ arg.size();
					}
				});

		// setup the idle system workload
		space.add(systemLoadAxis, new ArrayList<Workload>());

		// setup workload with disk IO kernels on all but the first core
		space.add(systemLoadAxis,
				createDiskIoWorkloads(tail(systemInfoRepository
						.getPossibleCPUs())));

		// setup workload with disk IO kernel son all cores
		space.add(systemLoadAxis,
				createDiskIoWorkloads(systemInfoRepository.getPossibleCPUs()));

		// setup workload with add kernels on all but the first core
		space.add(
				systemLoadAxis,
				createAddWorkloads(tail(systemInfoRepository.getPossibleCPUs())));

		// setup workload with add kernels on all cores
		space.add(systemLoadAxis,
				createAddWorkloads(systemInfoRepository.getPossibleCPUs()));

		space.add(clockTypeAxis, ClockType.CoreCycles);
		space.add(clockTypeAxis, ClockType.ReferenceCycles);
		space.add(clockTypeAxis, ClockType.uSecs);

		for (Coordinate coordinate : space) {
			boolean enoughIterations = false;
			for (long iterations = 1; !enoughIterations; iterations *= 2) {
				Measurement measurement = new Measurement();
				ArrayList<Workload> systemLoadWorkloads = coordinate
						.get(systemLoadAxis);

				// add system load workloads to measurement
				for (Workload loadWorkload : systemLoadWorkloads) {
					measurement.addWorkload(loadWorkload);
				}

				// setup main workload
				Workload mainWorkload = new Workload();
				measurement.addWorkload(mainWorkload);

				ArithmeticKernel arithmeticKernel = new ArithmeticKernel();
				arithmeticKernel.setOptimization("-O3");
				arithmeticKernel.setIterations(iterations);
				arithmeticKernel
						.setOperation(ArithmeticOperation.ArithmeticOperation_ADD);

				mainWorkload.setKernel(arithmeticKernel);

				QuantityCalculator<Time> addCalc = quantityMeasuringService
						.getExecutionTimeCalculator(coordinate
								.get(clockTypeAxis));
				mainWorkload.setMeasurerSet(single(addCalc
						.getRequiredMeasurerSets()));

				// setup stop rules for system load workloads
				for (Workload loadWorkload : systemLoadWorkloads) {
					WorkloadStopRule stopRule = new WorkloadStopRule();
					stopRule.setWorkload(mainWorkload);
					stopRule.setAction(new StopKernelAction(loadWorkload
							.getKernel()));
					measurement.addRule(stopRule);
				}

				// setup rule for waiting for system load workloads
				for (Workload loadWorkload : systemLoadWorkloads) {
					WorkloadStartRule startRule = new WorkloadStartRule();
					startRule.setWorkload(mainWorkload);
					startRule
							.setAction(new WaitForWorkloadAction(loadWorkload));
					measurement.addRule(startRule);
				}

				// perform measurement
				MeasurementResult result = measurementService.measure(
						measurement, 1);

				Time executionTime = addCalc.getResult(result
						.getMeasurerSetOutputs(single(addCalc
								.getRequiredMeasurerSets())));
				System.out.printf("%s: Iterations: %d Execution time: %s\n",
						coordinate, iterations, executionTime);

				// check if there are enough iterations already
				switch (coordinate.get(clockTypeAxis)) {
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
			kernel.setRunUntilStopped(true);
			workload.setKernel(kernel);
			result.add(workload);
		}
		return result;
	}
}
