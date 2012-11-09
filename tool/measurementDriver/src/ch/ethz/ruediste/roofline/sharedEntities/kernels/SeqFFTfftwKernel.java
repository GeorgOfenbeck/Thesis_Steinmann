package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.Operation;
import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

public class SeqFFTfftwKernel extends SeqFFTfftwKernelData {

	@Override
	public String getAdditionalLibraries(SystemInformation systemInformation) {
		return "-lfftw3 -lm";
	}

	@Override
	public String getLabelOverride() {
		return "Sequential non-vectorized FFT-FFTW";
	}

	@Override
	public Operation getSuggestedOperation() {
		return Operation.DoublePrecisionFlop;
	}
}