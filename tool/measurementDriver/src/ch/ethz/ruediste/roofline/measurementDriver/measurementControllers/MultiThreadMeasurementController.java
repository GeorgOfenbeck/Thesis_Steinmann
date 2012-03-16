package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;
import java.util.Map;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.actions.CreateMeasurerOnThreadAction;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.*;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.MeasurementRunEventPredicate.MeasurementRunEventEnum;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.WorkloadEventPredicate.WorkloadEventEnum;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

import com.google.inject.Inject;

public class MultiThreadMeasurementController implements IMeasurementController {

	public String getName() {
		return "multiThread";
	}

	public String getDescription() {
		return "runs a multithreaded measurement";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	public void measure(String outputName) throws IOException {
		final MMMKernel kernel = new MMMKernel();
		kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blas_Openblas);
		kernel.setMatrixSize(200);
		kernel.setOptimization("-O3");

		QuantityCalculator<OperationCount> opCountCalc = quantityMeasuringService
				.getOperationCountCalculator(Operation.CompInstr);
		QuantityCalculator<Time> execTimeCalc = quantityMeasuringService
				.getExecutionTimeCalculator(ClockType.CoreCycles);
		QuantityCalculator<TransferredBytes> tbCalc = quantityMeasuringService
				.getTransferredBytesCalculator(MemoryTransferBorder.LlcRam);

		IMeasurementBuilder builder = new IMeasurementBuilder() {

			public Measurement build(Map<String, MeasurerSet> sets) {
				Measurement measurement = new Measurement();
				Workload workload = new Workload();
				measurement.addWorkload(workload);
				workload.setKernel(kernel);
				workload.setMeasurerSet(sets.get("main"));

				CreateMeasurerOnThreadAction action = new CreateMeasurerOnThreadAction();
				measurement.addRule(new Rule(new MeasurementRunEventPredicate(
						MeasurementRunEventEnum.Start), action));

				action.setCreateOnExistingNonWorkloadThreads(true);
				action.setMeasurerSet(sets.get("main"));
				action.setStartPredicate(new WorkloadEventPredicate(workload,
						WorkloadEventEnum.KernelStart));
				action.setStopPredicate(new WorkloadEventPredicate(workload,
						WorkloadEventEnum.KernelStop));
				action.setReadPredicate(new WorkloadEventPredicate(workload,
						WorkloadEventEnum.Stop));
				action.setDisposePredicate(new MeasurementRunEventPredicate(
						MeasurementRunEventEnum.Stop));
				return measurement;
			}
		};

		QuantityMap quantities = quantityMeasuringService
				.measureQuantities(builder, 10)
				.with("main", opCountCalc, execTimeCalc, tbCalc).get();

		QuantityMap quantitiesExec = quantityMeasuringService
				.measureQuantities(kernel, 10, execTimeCalc);

		Time executionTime = quantitiesExec.min(execTimeCalc);

		System.out.printf("OpCount: %e\n", quantities.min(opCountCalc)
				.getValue());

		System.out.printf("execution time: %e\n",
				executionTime
						.getValue());
		System.out.printf("transferred bytes: %e\n", quantities.min(tbCalc)
				.getValue());

		Performance performance = new Performance(quantities.min(opCountCalc),
				executionTime);
		OperationalIntensity opIntens = new OperationalIntensity(
				quantities.min(tbCalc), quantities.min(opCountCalc));

		System.out.printf("performance: %e\n", performance.getValue());
		System.out.printf("opIntens: %e\n", opIntens.getValue());

	}
}
