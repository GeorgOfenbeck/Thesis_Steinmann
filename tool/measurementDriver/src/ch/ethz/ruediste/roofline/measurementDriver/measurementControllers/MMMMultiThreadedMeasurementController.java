package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.matrixSizeAxis;

import java.io.IOException;
import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.actions.CreateMeasurerOnThreadAction;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.*;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.WorkloadEventPredicate.WorkloadEventEnum;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

import com.google.inject.Inject;

public class MMMMultiThreadedMeasurementController implements
		IMeasurementController {
	public String getName() {
		return "MMMthread";
	}

	public String getDescription() {
		return "creates a multi threaded MMM roofline";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	@Inject
	RooflineController rooflineController;

	@Inject
	Configuration configuration;

	public void measure(String outputName) throws IOException {
		rooflineController.setOutputName(outputName);
		rooflineController
				.setTitle("Matrix Matrix Multiplication, with Threading");
		rooflineController.addDefaultPeaks();

		ParameterSpace space = new ParameterSpace();
		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();

		{
			MMMKernel kernel = new MMMKernel();
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blas_Mkl);
			kernel.setMatrixSize(200);
			kernel.setOptimization("-O3");

			space.add(Axes.kernelAxis, kernel);
			kernelNames.put(kernel, "MMM-Mkl");
		}

		{
			MMMKernel kernel = new MMMKernel();
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blas_Mkl);
			kernel.setMultiThreaded(true);
			kernel.setMatrixSize(200);
			kernel.setOptimization("-O3");

			space.add(Axes.kernelAxis, kernel);
			kernelNames.put(kernel, "MMM-Mkl-Threaded");
		}

		/*for (long size = 400; size <= 700; size += 100)
			space.add(Axes.matrixSizeAxis, size);
			*/
		space.add(matrixSizeAxis, 100L);

		configuration.push();
		for (Coordinate coordinate : space.getAllPoints(Axes.kernelAxis, null)) {
			int numRuns;
			if (coordinate.get(Axes.matrixSizeAxis) > 200) {
				configuration.set(QuantityMeasuringService.numberOfRunsKey, 1);
				numRuns = 1;
			}
			else {
				configuration.set(QuantityMeasuringService.numberOfRunsKey, 10);
				numRuns = 10;
			}

			final KernelBase kernel = coordinate.get(Axes.kernelAxis);
			kernel.initialize(coordinate);

			QuantityCalculator<OperationCount> opCountCalc = quantityMeasuringService
					.getOperationCountCalculator(Operation.DoublePrecisionFlop);
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

					// create predicates
					WorkloadEventPredicate startPredicate = new WorkloadEventPredicate(
							workload,
							WorkloadEventEnum.KernelStart);

					WorkloadEventPredicate stopPredicate = new WorkloadEventPredicate(
							workload,
							WorkloadEventEnum.KernelStop);

					// configure create measurer action
					{
						CreateMeasurerOnThreadAction action = new CreateMeasurerOnThreadAction();
						measurement.addRule(new Rule(startPredicate, action));

						action.setMeasurerSet(sets.get("main"));

						action.setStartPredicate(null);
						action.setStopPredicate(stopPredicate);
						action.setReadPredicate(stopPredicate);
						action.setDisposePredicate(stopPredicate);
					}

					return measurement;
				}
			};

			QuantityMap quantities = quantityMeasuringService
					.measureQuantities(builder, numRuns)
					.with("main", opCountCalc, execTimeCalc, tbCalc).get();

			QuantityMap quantitiesExec = quantityMeasuringService
					.measureQuantities(kernel, numRuns, execTimeCalc);

			Time executionTime = quantitiesExec.min(execTimeCalc);

			System.out.printf("OpCount: %f\n", quantities.min(opCountCalc)
					.getValue());

			rooflineController.addRooflinePoint(kernelNames.get(kernel),
					coordinate.get(Axes.matrixSizeAxis).toString(),
					quantities.min(opCountCalc), quantities.min(tbCalc),
					executionTime);
		}
		configuration.pop();

		rooflineController.plot();
	}
}
