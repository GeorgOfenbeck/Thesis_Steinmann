package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.kernelAxis;

import java.io.IOException;
import java.util.*;

import org.apache.commons.exec.ExecuteException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.configuration.Configuration;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.RunQuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.actions.*;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.WorkloadStopEventPredicate;

import com.google.inject.Inject;

public class ValidateTransferredBytesMeasurementController extends
		ValidationMeasurementControllerBase implements IMeasurementController {

	public String getName() {
		return "valTB";
	}

	public String getDescription() {
		return "runs validation measurements of the transferred bytes";
	}

	@Inject
	QuantityMeasuringService quantityMeasuringService;

	@Inject
	MeasurementService measurementService;

	@Inject
	PlotService plotService;

	@Inject
	Configuration configuration;

	public void measure(String outputName) throws IOException {
		measureMem(outputName);
		measureArith(outputName);

		configuration.push();
		configuration.set(QuantityMeasuringService.useAltTBKey, true);
		measureMem(outputName + "ALT");
		measureArith(outputName + "ALT");

		configuration.pop();
	}

	public void measureArith(String outputName) throws ExecuteException,
			IOException {
		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();
		ParameterSpace space = new ParameterSpace();

		setupArithmeticKernels(space, kernelNames);

		// initialize plot
		DistributionPlot plotTbValues = new DistributionPlot();
		plotTbValues.setOutputName(outputName + "ArithTBValues")
				.setTitle("Memory Transfer Values").setLog()
				.setxLabel("expOperationCount").setxUnit("flop")
				.setyLabel("actualMemTransfer").setyUnit("bytes")
				.setKeyPosition(KeyPosition.TopRight);

		DistributionPlot plotTimeValues = new DistributionPlot();
		plotTimeValues.setOutputName(outputName + "ArithTimeValues")
				.setTitle("Memory Execution Time Values").setLog()
				.setxLabel("expOperationCount").setxUnit("flop")
				.setyLabel("executionTime").setyUnit("cycles");

		// iterate over space
		for (Coordinate coordinate : space) {
			// initialize the kernel
			final KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			// get the calculator for the transferred bytes
			QuantityCalculator<TransferredBytes> transferredBytesCalc = quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRam);

			QuantityCalculator<Time> executionTimeCalc = quantityMeasuringService
					.getExecutionTimeCalculator(ClockType.CoreCycles);

			IMeasurementBuilder builder = new IMeasurementBuilder() {

				public Measurement build(Map<String, MeasurerSet> sets) {
					Measurement measurement = new Measurement();
					Workload workload = new Workload();
					measurement.addWorkload(workload);
					workload.setKernel(kernel);
					workload.setMeasurerSet(sets.get("main"));
					return measurement;
				}
			};
			QuantityMap result = quantityMeasuringService
					.measureQuantities(builder, 10)
					.with("main", transferredBytesCalc, executionTimeCalc)
					.get();

			long expOpCount = (long) kernel.getExpectedOperationCount()
					.getValue();

			// fill plots
			for (TransferredBytes transferredBytes : result
					.get(transferredBytesCalc)) {
				plotTbValues.addValue(kernelNames.get(kernel), expOpCount,
						transferredBytes.getValue());
			}
			for (Time time : result.get(executionTimeCalc)) {
				plotTimeValues.addValue(kernelNames.get(kernel), expOpCount,
						time.getValue());
			}
		}
		plotService.plot(plotTbValues);
		plotService.plot(plotTimeValues);
	}

	/**
	 * @param outputName
	 * @throws ExecuteException
	 * @throws IOException
	 */
	public void measureMem(String outputName) throws ExecuteException,
			IOException {
		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();
		ParameterSpace space = new ParameterSpace();

		setupMemoryKernels(space, kernelNames);

		// initialize plot
		DistributionPlot plotValues = new DistributionPlot();
		plotValues.setOutputName(outputName + "Values")
				.setTitle("Transferred Bytes Values").setLog()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("actualMemTransfer/expMemTransfer").setyUnit("1")
				.setKeyPosition(KeyPosition.TopRight);

		DistributionPlot flushValues = new DistributionPlot();
		flushValues.setOutputName(outputName + "FlushValues")
				.setTitle("Transferred Bytes Flush Values").setLog()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("actualMemTransfer").setyUnit("1")
				.setKeyPosition(KeyPosition.TopLeft);

		DistributionPlot plotError = new DistributionPlot();
		plotError.setOutputName(outputName + "Error");
		plotError.setTitle("Transferred Bytes Error").setLogX()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("err(actualMemTransfer/expMemTransfer)")
				.setyUnit("%").setYRange(yErrorRange())
				.setKeyPosition(KeyPosition.TopRight);

		DistributionPlot plotMinValues = new DistributionPlot();
		plotMinValues.setOutputName(outputName + "MinValues");
		plotMinValues.setTitle("Transferred Bytes Min Values").setLog()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("actualMemTransfer10/expMemTransfer").setyUnit("1");

		DistributionPlot plotMinError = new DistributionPlot();
		plotMinError.setOutputName(outputName + "MinError");
		plotMinError.setTitle("Transferred Bytes Min Error").setLogX()
				.setxLabel("expMemTransfer").setxUnit("bytes")
				.setyLabel("err(actualMemTransfer10/expMemTransfer)")
				.setyUnit("%").setYRange(yErrorRange());

		// iterate over space
		for (Coordinate coordinate : space) {
			// initialize the kernel
			final KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			// get the calculator for the transferred bytes
			QuantityCalculator<TransferredBytes> calc = quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRam);

			QuantityCalculator<TransferredBytes> flushCalc = quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRam);

			IMeasurementBuilder builder = new IMeasurementBuilder() {

				public Measurement build(Map<String, MeasurerSet> sets) {
					Measurement measurement = new Measurement();
					Workload workload = new Workload();
					measurement.addWorkload(workload);
					workload.setKernel(kernel);
					workload.setMeasurerSet(sets.get("main"));

					measurement.addRule(new Rule(
							new WorkloadStopEventPredicate(workload),
							new MeasureActionExecutionAction(
									new FlushKernelBuffersAction(kernel), sets
											.get("flush"))));
					return measurement;
				}
			};
			// run the measurement
			QuantityMap result = quantityMeasuringService
					.measureQuantities(builder, 100).with("main", calc)
					.with("flush", flushCalc).get();

			// get the expected number of bytes transferred
			TransferredBytes expected = kernel.getExpectedTransferredBytes();

			fillDistributionPlotsExpected(kernelNames.get(kernel),
					expected.getValue(), plotValues, plotMinValues, result,
					calc);

			for (RunQuantityMap runMap : result.getRunMaps()) {
				flushValues.addValue(kernelNames.get(kernel), (long) expected
						.getValue(), runMap.get(flushCalc).getValue());
			}

		}

		fillErrorPlotExpected(plotValues, plotError);
		fillErrorPlotExpected(plotMinValues, plotMinError);

		plotService.plot(plotError);
		plotService.plot(plotValues);
		plotService.plot(plotMinError);
		plotService.plot(plotMinValues);
		plotService.plot(flushValues);
	}
}
