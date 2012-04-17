package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.Operation;

public class FFTnrKernel extends FFTnrKernelData {
	@Override
	public String getLabelOverride() {
		return "FFT-NR";
	}

	@Override
	public Operation getSuggestedOperation() {
		return Operation.CompInstr;
	}
}
