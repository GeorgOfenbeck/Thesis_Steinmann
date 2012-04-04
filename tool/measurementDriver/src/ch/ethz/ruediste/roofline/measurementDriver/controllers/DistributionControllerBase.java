package ch.ethz.ruediste.roofline.measurementDriver.controllers;

import java.io.IOException;
import java.util.*;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.Range;

import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.ArgBuilderGetQuantities;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.IMeasurementBuilder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.actions.*;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.*;
import ch.ethz.ruediste.roofline.sharedEntities.eventPredicates.WorkloadEventPredicate.WorkloadEventEnum;

import com.google.inject.Inject;

public abstract class DistributionControllerBase<TQuantity extends Quantity<TQuantity>> {

	private final DistributionPlot plotValues = new DistributionPlot();
	private final DistributionPlot plotMinValues = new DistributionPlot();
	private final DistributionPlot plotError = new DistributionPlot();
	private final DistributionPlot plotMinError = new DistributionPlot();
	private final HistogramPlot plotHist = new HistogramPlot();
	private final HistogramPlot plotMinHist = new HistogramPlot();

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public PlotService plotService;

	@Inject
	public SystemInfoService systemInfoService;

	double toError(double ratio) {
		if (ratio < 1)
			ratio = 1 / ratio;
		return 100 * (ratio - 1);
	}

	public void measure(String outputName, final List<Integer> cpus,
			Coordinate... kernelCoordinates)
			throws ExecuteException, IOException {
		measureEnter(outputName, cpus, kernelCoordinates);

		// initialize plot
		setupValuesPlot(outputName, getPlotValues());
		setupMinValuesPlot(outputName, getPlotMinValues());
		setupHistogramPlot(outputName, plotHist);
		setupMinHistogramPlot(outputName, plotMinHist);

		// iterate over kernels to be measured
		for (Coordinate kernelCoordinate : kernelCoordinates)
		{
			double time = 0;
			long problemSize = initialProblemSize();

			// repeat until execution time exceed a certain value
			while (shouldContinue(time, problemSize, kernelCoordinate)) {
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

						PressureBarrier startBarrier = new PressureBarrier(
								cpus.size());

						for (int i : cpus) {
							KernelBase kernel = kernels.get(i);
							MeasurerSet measurerSet = sets.get(i);

							Workload workload = createWorkload(measurement,
									kernel,
									measurerSet);

							workload.setCpu(cpus.get(i));

							measurement.addWorkload(workload);

							measurement
									.addRule(new Rule(
											new WorkloadEventPredicate(
													workload,
													WorkloadEventEnum.KernelStart),
											new WaitForPressureBarrierAction(
													startBarrier, 1)));
						}

						return measurement;
					}
				};

				ArrayList<QuantityCalculator<TQuantity>> calcs = new ArrayList<QuantityCalculator<TQuantity>>();

				// create the argument builder
				ArgBuilderGetQuantities argBuilder = quantityMeasuringService
						.measureQuantities(
								builder, 100).with("execTime", execTimeCalc);

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

				additionalResultProcessing(problemSize, result, kernels, calcs,
						cpus);

				// add the results to the output
				for (int i : cpus) {
					KernelBase kernel = kernels.get(i);

					String kernelName = kernel.getLabel();
					String histogramName = getHistogramName(kernel, problemSize);
					if (cpus.size() > 1) {
						kernelName += "-" + i;
						if (histogramName != null)
							histogramName += "-" + i;
					}
					double expected = getX(kernel, problemSize);

					fillDistributionPlots(kernelName, expected,
							getPlotValues(),
							getPlotMinValues(), plotHist, plotMinHist,
							histogramName, result,
							calcs.get(i));
				}

				// book keeping
				time = result.min(execTimeCalc).getValue();
				problemSize = grownProblemSize(problemSize);
			}
		}

		getPlotError().setYRange(yErrorRange());
		setupErrorPlot(outputName, getPlotError());

		fillErrorPlot(getPlotValues(), getPlotError());

		getPlotMinError().setYRange(yErrorRange());
		setupMinErrorPlot(outputName, getPlotMinError());

		fillErrorPlot(getPlotMinValues(), getPlotMinError());

		plotService.plot(getPlotValues());
		plotService.plot(getPlotMinValues());
		plotService.plot(getPlotError());
		plotService.plot(getPlotMinError());
		if (plotHist.getOutputName() != null)
			plotService.plot(plotHist);
		if (plotMinHist.getOutputName() != null)
			plotService.plot(plotMinHist);

		measureLeave();
	}

	protected void measureEnter(String outputName, List<Integer> cpus,
			Coordinate[] kernelCoordinates) {

	}

	protected void measureLeave() throws ExecuteException, IOException {

	}

	protected void additionalResultProcessing(long problemSize,
			QuantityMap result,
			ArrayList<KernelBase> kernels,
			ArrayList<QuantityCalculator<TQuantity>> calcs, List<Integer> cpus) {

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
	 * return if the measurement should continue
	 * 
	 * @param time
	 *            time in uSecs of the last measurement
	 * @param problemSize
	 *            size of the next problem
	 * @param kernelCoordinate
	 *            TODO
	 */
	protected boolean shouldContinue(double time, long problemSize,
			Coordinate kernelCoordinate) {
		return time < 1e5;
	}

	protected long grownProblemSize(long problemSize) {
		return problemSize * 2;
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

	public void setupHistogramPlot(String outputName,
			HistogramPlot plotHist) {
	}

	public void setupMinHistogramPlot(String outputName,
			HistogramPlot plotMinHist) {
	}

	public String getHistogramName(KernelBase kernel, long problemSize) {
		return null;
	}

	public abstract void fillDistributionPlots(String kernelName,
			double expected,
			DistributionPlot plotValues, DistributionPlot plotMinValues,
			HistogramPlot plotHist, HistogramPlot plotMinHist,
			String histogramName, QuantityMap result,
			QuantityCalculator<TQuantity> calc);

	public abstract void fillErrorPlot(DistributionPlot valuesPlot,
			DistributionPlot errorPlot);

	public DistributionPlot getPlotValues() {
		return plotValues;
	}

	public DistributionPlot getPlotMinValues() {
		return plotMinValues;
	}

	public DistributionPlot getPlotError() {
		return plotError;
	}

	public DistributionPlot getPlotMinError() {
		return plotMinError;
	}

}