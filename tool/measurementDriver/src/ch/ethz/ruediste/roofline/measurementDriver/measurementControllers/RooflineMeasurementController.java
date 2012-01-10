package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.ArithmeticKernelDescription;
import ch.ethz.ruediste.roofline.dom.MemoryLoadKernelDescription;
import ch.ethz.ruediste.roofline.dom.TriadKernelDescription;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
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

		{
			ArithmeticKernelDescription kernel = new ArithmeticKernelDescription();
			kernel.setIterations(1024 * 10);
			kernel.setOptimization("-O3");
			kernel.setUnroll(4);
			plot.addPeakPerformance(rooflineService.getPerformance(
					"Arithmetic", kernel));
		}

		{
			TriadKernelDescription kernel = new TriadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernel.setOptimization("-O3");
			plot.addPoint(rooflineService.getRooflinePoint("Triad", kernel));
		}

		plotService.plot(plot);
	}

}
