package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.sharedEntities.Axes.*;

import java.io.IOException;
import java.util.HashMap;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.controllers.RooflineController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.OperationCount;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.sharedEntities.KernelBase;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

import com.google.inject.Inject;

public class MMMOptimizationMeasurementController implements
		IMeasurementController {

	public String getName() {
		return "mmmOpt";
	}

	public String getDescription() {
		return "measures various MMM kernels for fixed matrix sizes";
	}

	@Inject
	RooflineController rooflineController;

	public void measure(String outputName) throws IOException {
		rooflineController
				.setTitle("MMM Optimization TL/Blocked/Blocked for Regs/MKL");
		rooflineController.addDefaultPeaks();

		ParameterSpace space = new ParameterSpace();

		space.add(optimizationAxis, "-O3");
		space.add(matrixSizeAxis, 96L);
		space.add(matrixSizeAxis, 304L);
		space.add(matrixSizeAxis, 496L);

		HashMap<KernelBase, String> kernelNames = new HashMap<KernelBase, String>();
		// add triple loop kernel
		{
			MMMKernel kernel = new MMMKernel();
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_TrippleLoop);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "Tripple Loop");
		}

		// add blocked kernel
		{
			MMMKernel kernel = new MMMKernel();
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blocked);
			kernel.setNb(16);
			kernel.setMu(1);
			kernel.setKu(1);
			kernel.setNu(1);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "Blocked");
		}

		// add blocked regs kernel
		{
			MMMKernel kernel = new MMMKernel();
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blocked);
			kernel.setNb(16);
			kernel.setMu(2);
			kernel.setKu(2);
			kernel.setNu(2);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "Blocked Regs");
		}

		// add mkl kernel
		// add triple loop kernel
		{
			MMMKernel kernel = new MMMKernel();
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blas_Mkl);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "Mkl");
		}

		for (Coordinate coordinate : space.getAllPoints(null, matrixSizeAxis)) {
			KernelBase kernel = coordinate.get(kernelAxis);
			kernel.initialize(coordinate);

			OperationCount opCount = new OperationCount(Math.pow(
					coordinate.get(matrixSizeAxis), 3) * 2);

			rooflineController.addRooflinePoint(coordinate.get(matrixSizeAxis)
					.toString(), kernelNames.get(kernel), kernel, opCount,
					MemoryTransferBorder.LlcRam);
		}
		rooflineController.plot();
	}

}
