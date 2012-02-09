package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.*;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController.Algorithm;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
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
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("MUL", Algorithm.Mul,
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("ABal", Algorithm.ArithBalanced,
				InstructionSet.SSEScalar);

		rooflineController.addPeakThroughput("MemLoad", Algorithm.Load,
				MemoryTransferBorder.LlcRam);

		{
			TriadKernel kernel = new TriadKernel();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernel.setOptimization("-O3");
			rooflineController.addRooflinePoint("Triad", kernel,
					Operation.CompInstr, MemoryTransferBorder.LlcRam);
		}

		{
			ParameterSpace space = new ParameterSpace();
			for (long i = 64; i <= 1024; i += 64) {
				space.add(Axes.matrixSizeAxis, i);
			}
			space.add(Axes.blockSizeAxis, 64L);

			space.add(Axes.optimizationAxis, "-O3");

			for (Coordinate coordinate : space) {
				MMMKernel kernel = new MMMKernel();
				kernel.setMu(2);
				kernel.setNu(2);
				kernel.setKu(2);
				kernel.setNoCheck(true);
				kernel.initialize(coordinate);

				rooflineController.addRooflinePoint(
						"MMM" + coordinate.get(Axes.matrixSizeAxis), kernel,
						Operation.CompInstr, MemoryTransferBorder.LlcRam);
			}
		}

		rooflineController.plot();
	}
}
