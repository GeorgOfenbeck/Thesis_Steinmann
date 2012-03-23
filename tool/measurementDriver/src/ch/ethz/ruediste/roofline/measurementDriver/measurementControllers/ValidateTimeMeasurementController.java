package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.head;
import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;
import java.util.*;

import org.apache.commons.exec.ExecuteException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Time;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.ArgBuilderGetQuantities;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.ArithmeticKernel.ArithmeticOperation;

import com.google.inject.Inject;

public class ValidateTimeMeasurementController extends
		ValidationMeasurementControllerBase implements IMeasurementController {

	public String getName() {
		return "valTime";
	}

	public String getDescription() {
		return "runs validation measurements of the execution time";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	@Inject
	MeasurementService measurementService;

	@Inject
	PlotService plotService;

	@Inject
	SystemInfoService systemInfoService;

	public void measure(String outputName) throws IOException {
		//measureMem(outputName);
		measureArith(outputName + "Add", systemInfoService.getOnlineCPUs(),
				createCoordinate(ArithmeticOperation.ArithmeticOperation_ADD,
						InstructionSet.SSE));

		List<Integer> singleCpu = Collections
				.singletonList(head(systemInfoService
						.getOnlineCPUs()));
		measureArith(
				outputName,
				singleCpu,
				createCoordinate(ArithmeticOperation.ArithmeticOperation_ADD,
						InstructionSet.SSE),
				createCoordinate(ArithmeticOperation.ArithmeticOperation_ADD,
						InstructionSet.x87),
				createCoordinate(ArithmeticOperation.ArithmeticOperation_MUL,
						InstructionSet.SSE),
				createCoordinate(ArithmeticOperation.ArithmeticOperation_MUL,
						InstructionSet.x87));
	}

	public Coordinate createCoordinate(ArithmeticOperation operation,
			InstructionSet instructionSet) {
		return Coordinate.EMPTY.getExtendedPoint(kernelClassAxis,
				ArithmeticKernel.class).getExtendedPoint(arithOperationAxis,
				operation)
				.getExtendedPoint(instructionSetAxis,
						instructionSet);
	}

	public void measureArith(String outputName, final List<Integer> cpus,
			Coordinate... kernelCoordinates)
			throws ExecuteException,
			IOException {

		// initialize plot
		DistributionPlot plotValues = new DistributionPlot();
		plotValues.setOutputName(outputName + "ArithValues")
				.setTitle("Time Values").setLog().setxLabel("expOperations")
				.setxUnit("operation").setyLabel("time").setyUnit("cycles");

		DistributionPlot plotMinValues = new DistributionPlot();
		plotMinValues.setOutputName(outputName + "ArithMinValues")
				.setTitle("Time Min Values").setLog().setxLabel("expOpCount")
				.setxUnit("1").setyLabel("min(time)").setyUnit("cycles");

		// iterate over kernels to be measured
		for (Coordinate kernelCoordinate : kernelCoordinates)
		{
			double time = 0;
			long iterations = 128;

			// repeat until execution time exceed a certain value
			while (time < 1e5) {
				QuantityCalculator<Time> execTimeCalc = quantityMeasuringService
						.getExecutionTimeCalculator(ClockType.uSecs);

				// space to store the kernel for each CPU
				final ArrayList<KernelBase> kernels = new ArrayList<KernelBase>();

				// create the builder
				IMeasurementBuilder builder = new IMeasurementBuilder() {
					public Measurement build(Map<Object, MeasurerSet> sets) {
						Measurement measurement = new Measurement();
						if (sets.containsKey("execTime"))
							measurement.setOverallMeasurerSet(sets
									.get("execTime"));

						for (int i : cpus) {
							Workload workload = new Workload();
							workload.setKernel(kernels.get(i));
							workload.setMeasurerSet(sets.get(i));
							measurement.addWorkload(workload);
						}

						return measurement;
					}
				};

				ArrayList<QuantityCalculator<Time>> calcs = new ArrayList<QuantityCalculator<Time>>();

				// create the argument builder
				ArgBuilderGetQuantities argBuilder = quantityMeasuringService
						.measureQuantities(
								builder, 10).with("execTime", execTimeCalc);

				// create the kernels and calculators for all cpus
				for (int i : cpus) {
					// get the calculator for the transferred bytes
					QuantityCalculator<Time> calc = quantityMeasuringService
							.getExecutionTimeCalculator(ClockType.CoreCycles);

					// initialize the kernel
					ArithmeticKernel kernel = (ArithmeticKernel) KernelBase
							.create(kernelCoordinate);
					kernel.setIterations(iterations);

					kernels.add(kernel);
					calcs.add(calc);
					argBuilder.with(i, calc);
				}

				// run the measurement
				QuantityMap result = argBuilder.get();

				// add the results to the output
				for (int i : cpus) {
					String kernelName = kernels.get(i).getLabel();
					if (cpus.size() > 1) {
						kernelName += i;
					}

					long expected = (long) kernels.get(i)
							.getExpectedOperationCount()
							.getValue();

					fillDistributionPlots(kernelName, expected, plotValues,
							plotMinValues, result, calcs.get(i));
				}

				// book keeping
				time = result.min(execTimeCalc).getValue();
				iterations *= 4;
			}
		}

		DistributionPlot plotError = new DistributionPlot();
		plotError.setOutputName(outputName + "ArithError")
				.setTitle("Time Error").setLogX().setxLabel("expOpCount")
				.setKeyPosition(KeyPosition.TopLeft).setxUnit("operations")
				.setyLabel("err(time/min(time))").setyUnit("%")
				.setYRange(yErrorRange());

		fillErrorPlotMin(plotValues, plotError);

		DistributionPlot plotMinError = new DistributionPlot();
		plotMinError.setOutputName(outputName + "ArithMinError")
				.setTitle("Time Min Error").setLogX().setxLabel("expOpCount")
				.setKeyPosition(KeyPosition.TopRight).setxUnit("operations")
				.setyLabel("err(time10/min(time10))").setyUnit("%")
				.setYRange(yErrorRange());

		fillErrorPlotMin(plotMinValues, plotMinError);

		plotService.plot(plotValues);
		plotService.plot(plotMinValues);
		plotService.plot(plotError);
		plotService.plot(plotMinError);
	}

	public void measureMem(String outputName) throws ExecuteException,
			IOException {
		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();
		ParameterSpace space = new ParameterSpace();

		setupMemoryKernels(space, kernelNames);

		// initialize plot
		DistributionPlot plotValues = new DistributionPlot();
		plotValues.setOutputName(outputName + "MemValues")
				.setTitle("Time Values").setLog().setxLabel("expMemTransfer")
				.setxUnit("bytes").setyLabel("time").setyUnit("cycles");

		DistributionPlot plotMinValues = new DistributionPlot();
		plotMinValues.setOutputName(outputName + "MemMinValues")
				.setTitle("Time Min Values").setLog()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("time10").setyUnit("cycles");

		// iterate over space
		for (Coordinate coordinate : space) {
			// get the calculator for the transferred bytes
			QuantityCalculator<Time> calc = quantityMeasuringService
					.getExecutionTimeCalculator(ClockType.CoreCycles);

			// initialize the kernel
			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			QuantityMap result = quantityMeasuringService.measureQuantities(
					kernel, 100, calc);

			long expTransBytes = (long) kernel.getExpectedTransferredBytes()
					.getValue();
			String kernelName = kernelNames.get(kernel);

			fillDistributionPlots(kernelName, expTransBytes, plotValues,
					plotMinValues, result, calc);
		}

		DistributionPlot plotError = new DistributionPlot();
		plotError.setOutputName(outputName + "MemError");
		plotError.setTitle("Time Error").setLogX().setxLabel("expMemTransfer")
				.setxUnit("bytes").setyLabel("err(time/min(time))")
				.setyUnit("\\%").setYRange(yErrorRange())
				.setKeyPosition(KeyPosition.TopRight);

		DistributionPlot plotMinError = new DistributionPlot();
		plotMinError.setOutputName(outputName + "MemMinError")
				.setTitle("Time Min Error").setLogX()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("err(time10/min(time10))").setyUnit("\\%")
				.setYRange(yErrorRange()).setKeyPosition(KeyPosition.TopRight);

		fillErrorPlotMin(plotValues, plotError);

		fillErrorPlotMin(plotMinValues, plotMinError);

		plotService.plot(plotValues);
		plotService.plot(plotMinValues);
		plotService.plot(plotError);
		plotService.plot(plotMinError);
	}
}
