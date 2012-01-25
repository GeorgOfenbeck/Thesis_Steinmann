package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.*;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController.Algorithm;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.Operation;

import com.google.inject.Inject;

public class RooflineMeasurementController implements IMeasurementController {

	public String getName() {
		return "roofline";
	}

	public String getDescription() {
		return "create a roofline plot";
	}

	@Inject
	RooflineController rooflineController;

	public void measure(String outputName) throws IOException {
		/*rooflineController.addPeakPerformance("ADD x87", Algorithm.Add,
				InstructionSet.x87);

		rooflineController.addPeakPerformance("ADD SSE", Algorithm.Add,
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("MUL x87", Algorithm.Mul,
				InstructionSet.x87);
		rooflineController.addPeakPerformance("MUL SSE", Algorithm.Mul,
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("ABal x87",
				Algorithm.ArithBalanced, InstructionSet.x87);
		rooflineController.addPeakPerformance("ABal SSE",
				Algorithm.ArithBalanced, InstructionSet.SSE);
				*/

		rooflineController.addPeakPerformance("ADD", Algorithm.Add,
				InstructionSet.SSEScalar);
		rooflineController.addPeakPerformance("MUL", Algorithm.Mul,
				InstructionSet.SSEScalar);
		rooflineController.addPeakPerformance("ABal",
				Algorithm.ArithBalanced, InstructionSet.SSEScalar);

		rooflineController.addPeakThroughput("MemLoad", Algorithm.Load,
				MemoryTransferBorder.LlcRam);

		{
			TriadKernelDescription kernel = new TriadKernelDescription();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernel.setOptimization("-O3");
			rooflineController.addRooflinePoint("Triad", kernel, Operation.x87,
					MemoryTransferBorder.LlcRam);
		}

		rooflineController.plot();
	}
}
