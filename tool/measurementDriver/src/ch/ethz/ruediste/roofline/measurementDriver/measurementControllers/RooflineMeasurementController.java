package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import java.io.IOException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;
import ch.ethz.ruediste.roofline.measurementDriver.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

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
		/*rooflineController.addPeakPerformance("ADD x87", PeakAlgorithm.Add,
				InstructionSet.x87);

		rooflineController.addPeakPerformance("ADD SSE", PeakAlgorithm.Add,
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("MUL x87", PeakAlgorithm.Mul,
				InstructionSet.x87);
		rooflineController.addPeakPerformance("MUL SSE", PeakAlgorithm.Mul,
				InstructionSet.SSE);
		rooflineController.addPeakPerformance("ABal x87",
				PeakAlgorithm.ArithBalanced, InstructionSet.x87);
		rooflineController.addPeakPerformance("ABal SSE",
				PeakAlgorithm.ArithBalanced, InstructionSet.SSE);
		 */

		rooflineController.addDefaultPeaks();

		{
			TriadKernel kernel = new TriadKernel();
			kernel.setBufferSize(1024 * 1024 * 2);
			kernel.setOptimization("-O3");
			rooflineController.addRooflinePoint("Triad", "", kernel,
					Operation.CompInstr, MemoryTransferBorder.LlcRam);
		}

		{
			ParameterSpace space = new ParameterSpace();
			for (long i = 64; i <= 900; i += 64) {
				space.add(Axes.matrixSizeAxis, i);
			}
			space.add(Axes.blockSizeAxis, 64L);

			space.add(Axes.optimizationAxis, "-O3");
			Axis<MMMAlgorithm> algorithmAxis = new Axis<MMMKernel.MMMAlgorithm>(
					"742250a7-5ea2-4a39-b0c6-7145d0c4b292", "algorithm");

			space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_TripleLoop);
			space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_Blocked);
			space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_Blas_Openblas);
			space.add(algorithmAxis, MMMAlgorithm.MMMAlgorithm_Blas_Mkl);

			for (Coordinate coordinate : space.getAllPoints(space
					.getAllAxesWithLeastSignificantAxes(algorithmAxis))) {
				MMMKernel kernel = new MMMKernel();
				kernel.setMu(2);
				kernel.setNu(2);
				kernel.setKu(2);
				kernel.setNoCheck(true);
				kernel.setAlgorithm(coordinate.get(algorithmAxis));
				kernel.initialize(coordinate);

				String name = "MMM" + coordinate.get(algorithmAxis)
						+ coordinate.get(Axes.matrixSizeAxis);
				System.out.printf("Measuring %s\n", name);

				Operation operation = Operation.CompInstr;
				if (coordinate.get(algorithmAxis) == MMMAlgorithm.MMMAlgorithm_Blas_Openblas) {

					operation = Operation.DoublePrecisionFlop;
				}
				rooflineController.addRooflinePoint(
						coordinate.get(algorithmAxis).toString(), coordinate
								.get(Axes.matrixSizeAxis).toString(), kernel,
						operation, MemoryTransferBorder.LlcRam);
			}
		}

		rooflineController.plot();
	}
}
