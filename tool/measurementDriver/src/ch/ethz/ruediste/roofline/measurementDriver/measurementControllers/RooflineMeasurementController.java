package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.ArithmeticKernelDescription;
import ch.ethz.ruediste.roofline.dom.MemoryLoadKernelDescription;
import ch.ethz.ruediste.roofline.dom.TriadKernelDescription;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.Performance;
import ch.ethz.ruediste.roofline.measurementDriver.dom.RooflinePlot;
import ch.ethz.ruediste.roofline.measurementDriver.services.PlotService;
import ch.ethz.ruediste.roofline.measurementDriver.services.RooflineService;

import com.google.inject.Inject;

public class RooflineMeasurementController implements IMeasurementController {

	public String getName() {
		return "roofline";
	}

	public String getDescription() {
		return "create a roofline plot";
	}

	@Inject
	RooflineService rooflineService;

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

		String optimization = "-O3 -mtune=core2";
		
		{
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.setIterations(1000000);
			kernel.setOptimization(optimization);
			kernel.setUnroll(3);
			kernel.setOperation("ArithmeticOperation_MULADD");
			Performance performance = rooflineService.getPerformance(
					"Balanced", kernel);
			plot.addPeakPerformance(performance);
			plot.addPeakPerformance(new Performance("thBal",kernel.getIterations()*kernel.getUnroll()*3, performance.getTime()));
		}

		{
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.setIterations(1000000);
			
			kernel.setOptimization(optimization);
			kernel.setUnroll(4);
			kernel.setOperation("ArithmeticOperation_ADD");
			plot.addPeakPerformance(rooflineService.getPerformance(
					"Additions", kernel));
		}

		{
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.setIterations(1000000);
			kernel.setOptimization(optimization);
			kernel.setUnroll(4);
			kernel.setOperation("ArithmeticOperation_MUL");
			plot.addPeakPerformance(rooflineService.getPerformance(
					"Multiplications", kernel));
		}

		{
			TriadKernelDescription kernel = new TriadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernel.setOptimization("-O3 -msse2");
			plot.addPoint(rooflineService.getRooflinePoint("Triad", kernel));
		}

		plotService.plot(plot);
	}

}
