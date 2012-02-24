package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.entities.Axes.*;
import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.single;

import java.io.IOException;
import java.util.*;

import ch.ethz.ruediste.roofline.entities.MeasurementResult;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.TransferredBytes;
import ch.ethz.ruediste.roofline.measurementDriver.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MemoryKernel.MemoryOperation;

import com.google.inject.Inject;

public class ValidateTransferredBytesMeasurementController implements
		IMeasurementController {

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

	public void measure(String outputName) throws IOException {
		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();
		ParameterSpace space = new ParameterSpace();

		// setup read kernel
		{
			MemoryKernel kernel = new MemoryKernel();
			kernel.setUnroll(1);
			kernel.setDlp(1);
			kernel.setOptimization("-O3 -msse2");
			kernel.setPrefetchDistance(0L);
			kernel.setOperation(MemoryOperation.MemoryOperation_READ);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "Read");
		}

		// setup write kernel
		{
			MemoryKernel kernel = new MemoryKernel();
			kernel.setUnroll(2);
			kernel.setDlp(1);
			kernel.setOptimization("-O3 -msse2");
			kernel.setPrefetchDistance(0L);
			kernel.setOperation(MemoryOperation.MemoryOperation_WRITE);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "Write");
		}

		// setup buffer sizes
		for (long i = 128; i < 1024 * 1024 * 20; i *= 2) {
			space.add(bufferSizeAxis, i);
		}

		// initialize plot
		DistributionPlot plot = new DistributionPlot();
		plot.setOutputName(outputName);
		plot.setTitle("Memory Error");
		plot.setxLabel("buffer size");
		plot.setyLabel("actual/expected");
		plot.setxUnit("floats");
		plot.setyUnit("1");

		// iterate over space
		for (Coordinate coordinate : space) {
			// initialize the kernel
			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			// get the calculator for the transferred bytes
			QuantityCalculator<TransferredBytes> calc = quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRam);
			MeasurerBase measurer = single(calc.getRequiredMeasurers());

			// setup the measurement
			Measurement measurement = new Measurement();
			Workload workload = new Workload();
			measurement.addWorkload(workload);
			workload.setKernel(kernel);
			workload.setMeasurerSet(new MeasurerSet(measurer));

			// run the measurement
			MeasurementResult result = measurementService.measure(measurement,
					100);

			// get the expected number of bytes transferred
			TransferredBytes expected = kernel.getExpectedTransferredBytes();

			// print results to console and fill plot
			/*System.out.printf("%s %s: expected: %s\n", kernelNames.get(kernel),
					coordinate, expected);*/
			for (MeasurementRunOutput runOutput : result.getOutputs()) {
				TransferredBytes actual = calc.getResult(Collections
						.singletonList(runOutput.getMeasurerOutput(measurer)));
				double ratio = actual.getValue() / expected.getValue();
				//System.out.printf("%s -> %g\n", actual, ratio);

				if (ratio < 1) {
					ratio = 1 / ratio;
				}
				plot.addValue(kernelNames.get(kernel),
						coordinate.get(bufferSizeAxis), 100 * (ratio - 1));
			}

		}

		plotService.plot(plot);
	}

}
