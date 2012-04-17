package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.Operation;

public class FFTSpiralKernel extends FFTSpiralKernelData {
	@Override
	public String getLabel() {
		return "FFT-Spiral";
	}

	@Override
	public Operation getSuggestedOperation() {
		return Operation.DoublePrecisionFlop;
	}
}
