package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.single;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.dom.ArithmeticKernel.ArithmeticOperation;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Time;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.ClockType;

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

	public void measure(String outputName) throws IOException {
		Measurement measurement = new Measurement();

		// setup disk IO workload
		Workload diskIoWorkload = new Workload();
		measurement.addWorkload(diskIoWorkload);

		DiskIoKernel diskIoKernel = new DiskIoKernel();
		diskIoKernel.setFileSize(1024 * 1024 * 1L);
		diskIoKernel.setIterations(-1);
		diskIoWorkload.setKernel(diskIoKernel);
		MeasurerSet diskIoMeasurerSet = new MeasurerSet();
		diskIoWorkload.setMeasurerSet(diskIoMeasurerSet);

		ExecutionTimeMeasurer diskIoMeasurer = new ExecutionTimeMeasurer();
		diskIoMeasurerSet.setMainMeasurer(diskIoMeasurer);

		// setup ADD workload
		Workload addWorkload = new Workload();
		measurement.addWorkload(addWorkload);

		ArithmeticKernel arithmeticKernel = new ArithmeticKernel();
		arithmeticKernel.setOptimization("-O3");
		arithmeticKernel.setInstructionSet(InstructionSet.x87);
		arithmeticKernel.setIterations(100000000);
		arithmeticKernel.setDlp(3);
		arithmeticKernel.setUnroll(3);
		arithmeticKernel
				.setOperation(ArithmeticOperation.ArithmeticOperation_ADD);

		addWorkload.setKernel(arithmeticKernel);

		QuantityCalculator<Time> addCalc = quantityMeasuringService
				.getExecutionTimeCalculator(ClockType.CoreCycles);
		addWorkload.setMeasurerSet(single(addCalc.getRequiredMeasurerSets()));

		// setup stop rule
		WorkloadStopRule stopRule = new WorkloadStopRule();
		stopRule.setWorkload(addWorkload);
		stopRule.setAction(new StopKernelAction(diskIoKernel));

		measurement.getRules().add(stopRule);

		MeasurementResult result = measurementService.measure(measurement, 1);

		System.out.printf("Execution time disk IO[usec]: %d\n",
				single(result.getMeasurerOutputs(diskIoMeasurer)).getUSecs());

		System.out.printf("Execution time add [cycles]: %s\n", addCalc
				.getResult(result.getMeasurerSetOutputs(single(addCalc
						.getRequiredMeasurerSets()))));

	}
}
