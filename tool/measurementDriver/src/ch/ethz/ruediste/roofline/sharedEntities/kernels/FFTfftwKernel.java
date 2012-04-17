package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.*;

public class FFTfftwKernel extends FFTfftwKernelData {

	@Override
	public String getAdditionalLibraries(SystemInformation systemInformation) {
		return "-lfftw3_threads -lfftw3 -lm";
	}

	@Override
	public String getLabelOverride() {
		return "FFT-FFTW";
	}

	@Override
	public Operation getSuggestedOperation() {
		return Operation.DoublePrecisionFlop;
	}

}
