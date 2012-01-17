package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllerHelpers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.services.PlotService;

import com.google.inject.Inject;

public class RooflineMeasurementController implements IMeasurementController {

	public String getName() {
		return "roofline";
	}

	public String getDescription() {
		return "create a roofline plot";
	}

	@Inject
	RooflineController rooflineService;

	@Inject
	PlotService plotService;

	public void measure(String outputName) throws IOException {
		RooflinePlot plot = new RooflinePlot();
		plot.setOutputName("roofline");
		plot.setTitle("A Roofline Plot");
		plot.setxLabel("Operational Intensity");
		plot.setxUnit("Operations/Byte");
		plot.setyLabel("Performance");
		plot.setyUnit("flops/cycle");

		{
			MemoryLoadKernelDescription kernel = new MemoryLoadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernel.setOptimization("-O3 -msse");
			plot.addPeakBandwidth(rooflineService.getMemoryBandwidth("MemLoad",
					kernel));
		}

		String optimization = "-O3";

		{
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.setIterations(1000000);
			kernel.setOptimization(optimization);
			kernel.setDlp(2);
			kernel.setUnroll(6);
			kernel.setOperation("ArithmeticOperation_MULADD");
			Performance performance = rooflineService.getPerformance(
					"Balanced", kernel);
			plot.addPeakPerformance(performance);
			/*
			 * plot.addPeakPerformance(new Performance("thBal", kernel
			 * .getIterations() * kernel.getUnroll() * 3 kernel.getDlp(),
			 * performance .getTime()));
			 */
		}

		{
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.setIterations(100000);

			kernel.setOptimization(optimization);
			kernel.setUnroll(19);
			kernel.setDlp(7);
			kernel.setOperation("ArithmeticOperation_ADD");
			plot.addPeakPerformance(rooflineService.getPerformance("Additions",
					kernel));
		}

		{
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.setIterations(100000);
			kernel.setOptimization(optimization);
			kernel.setDlp(16);
			kernel.setUnroll(18);
			kernel.setOperation("ArithmeticOperation_MUL");
			plot.addPeakPerformance(rooflineService.getPerformance(
					"Multiplications", kernel));
		}

		{
			TriadKernelDescription kernel = new TriadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernel.setOptimization("-O3");
			RooflinePoint rooflinePoint = rooflineService.getRooflinePoint(
					"Triad", kernel);
			plot.addPoint(rooflinePoint);
			System.out.println(rooflinePoint);
		}

		plotService.plot(plot);
	}
}
