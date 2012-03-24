package ch.ethz.ruediste.roofline.measurementDriver.controllers;

import java.io.IOException;
import java.util.*;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.Range;

import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.DistributionPlot;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.ArgBuilderGetQuantities;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;

import com.google.inject.Inject;

public abstract class DistributionControllerBase<TQuantity extends Quantity<TQuantity>> {

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public PlotService plotService;

	double toError(double ratio) {
		if (ratio < 1)
			ratio = 1 / ratio;
		return 100 * (ratio - 1);
	}

	public void measure(String outputName, final List<Integer> cpus,
			Coordinate... kernelCoordinates)
			throws ExecuteException, IOException {

		// initialize plot
		DistributionPlot plotValues = new DistributionPlot();
		setupValuesPlot(outputName, plotValues);

		DistributionPlot plotMinValues = new DistributionPlot();
		setupMinValuesPlot(outputName, plotMinValues);

		// iterate over kernels to be measured
		for (Coordinate kernelCoordinate : kernelCoordinates)
		{
			double time = 0;
			long problemSize = initialProblemSize();

			// repeat until execution time exceed a certain value
			while (time < maxTime()) {
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
							KernelBase kernel = kernels.get(i);
							MeasurerSet measurerSet = sets.get(i);

							Workload workload = createWorkload(measurement,
									kernel,
									measurerSet);

							measurement.addWorkload(workload);
						}

						return measurement;
					}
				};

				ArrayList<QuantityCalculator<TQuantity>> calcs = new ArrayList<QuantityCalculator<TQuantity>>();

				// create the argument builder
				ArgBuilderGetQuantities argBuilder = quantityMeasuringService
						.measureQuantities(
								builder, 10).with("execTime", execTimeCalc);

				// create the kernels and calculators for all cpus
				for (int i : cpus) {
					// initialize the kernel
					KernelBase kernel = createKernel(
							kernelCoordinate, problemSize);

					// get the calculator
					QuantityCalculator<TQuantity> calc = createCalculator(kernel);

					kernels.add(kernel);
					calcs.add(calc);
					argBuilder.with(i, calc);
				}

				// run the measurement
				QuantityMap result = argBuilder.get();

				// add the results to the output
				for (int i : cpus) {
					KernelBase kernel = kernels.get(i);

					String kernelName = kernel.getLabel();
					if (cpus.size() > 1) {
						kernelName += i;
					}

					double expected = getX(kernel, problemSize);

					fillDistributionPlots(kernelName, expected, plotValues,
							plotMinValues, result, calcs.get(i));
				}

				// book keeping
				time = result.min(execTimeCalc).getValue();
				problemSize = grownProblemSize(problemSize);
			}
		}

		DistributionPlot plotError = new DistributionPlot();
		plotError.setYRange(yErrorRange());
		setupErrorPlot(outputName, plotError);

		fillErrorPlot(plotValues, plotError);

		DistributionPlot plotMinError = new DistributionPlot();
		plotMinError.setYRange(yErrorRange());
		setupMinErrorPlot(outputName, plotMinError);

		fillErrorPlot(plotMinValues, plotMinError);

		plotService.plot(plotValues);
		plotService.plot(plotMinValues);
		plotService.plot(plotError);
		plotService.plot(plotMinError);
	}

	private Range<Double> yErrorRange() {
		return Range.between(0., 50.);
	}

	protected abstract double getX(KernelBase kernel, long problemSize);

	protected Workload createWorkload(Measurement measurement,
			KernelBase kernel,
			MeasurerSet measurerSet) {
		Workload workload = new Workload();
		workload.setKernel(kernel);
		workload.setMeasurerSet(measurerSet);
		return workload;
	}

	protected abstract KernelBase createKernel(Coordinate kernelCoordinate,
			long problemSize);

	/**
	 * return the maximal time for a measurement run
	 */
	protected double maxTime() {
		return 1e5;
	}

	protected long grownProblemSize(long problemSize) {
		return problemSize * 4;
	}

	protected long initialProblemSize() {
		return 128;
	}

	protected abstract QuantityCalculator<TQuantity> createCalculator(
			KernelBase kernel);

	public abstract void setupValuesPlot(String outputName,
			DistributionPlot plotValues);

	public abstract void setupMinValuesPlot(String outputName,
			DistributionPlot plotMinValues);

	public abstract void setupErrorPlot(String outputName,
			DistributionPlot plotError);

	public abstract void setupMinErrorPlot(String outputName,
			DistributionPlot plotMinError);

	public abstract void fillDistributionPlots(String kernelName,
			double expected,
			DistributionPlot plotValues, DistributionPlot plotMinValues,
			QuantityMap result, QuantityCalculator<TQuantity> calc);

	public abstract void fillErrorPlot(DistributionPlot valuesPlot,
			DistributionPlot errorPlot);

}