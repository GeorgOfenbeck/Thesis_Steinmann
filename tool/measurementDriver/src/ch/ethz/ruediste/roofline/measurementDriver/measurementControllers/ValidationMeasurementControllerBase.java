package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.entities.Axes.*;

import java.util.HashMap;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.ArithmeticKernel.ArithmeticOperation;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MemoryKernel.MemoryOperation;

public class ValidationMeasurementControllerBase {

	public ValidationMeasurementControllerBase() {
		super();
	}

	/**
	 * @param space
	 * @param kernelNames
	 */
	public void setupMemoryKernels(ParameterSpace space,
			HashMap<KernelBase, String> kernelNames) {
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

		// setup triad kernel
		{
			TriadKernel kernel = new TriadKernel();
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "Triad");
		}

		// setup buffer sizes
		for (long i = 128; i < 1024 * 1024 * 2; i *= 2) {
			space.add(bufferSizeAxis, i);
		}
	}

	/**
	 * @param space
	 * @param kernelNames
	 */
	public void setupArithmeticKernels(ParameterSpace space,
			HashMap<KernelBase, String> kernelNames) {
		// setup add sse kernel
		{
			ArithmeticKernel kernel = new ArithmeticKernel();
			kernel.setUnroll(4);
			kernel.setDlp(2);
			kernel.setOptimization("-O3 -msse2");
			kernel.setOperation(ArithmeticOperation.ArithmeticOperation_ADD);
			kernel.setInstructionSet(InstructionSet.SSE);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "ADD SSE");
		}

		// setup add x87 kernel
		{
			ArithmeticKernel kernel = new ArithmeticKernel();
			kernel.setUnroll(4);
			kernel.setDlp(2);
			kernel.setOptimization("-O3");
			kernel.setOperation(ArithmeticOperation.ArithmeticOperation_ADD);
			kernel.setInstructionSet(InstructionSet.x87);
			space.add(kernelAxis, kernel);
			kernelNames.put(kernel, "ADD x87");
		}

		// setup iteration counts
		for (long i = 128; i < 1024 * 1024 * 128L; i *= 4) {
			space.add(iterationsAxis, i);
		}
	}

	double toError(double ratio) {
		if (ratio < 1)
			ratio = 1 / ratio;
		return 100 * (ratio - 1);
	}
}