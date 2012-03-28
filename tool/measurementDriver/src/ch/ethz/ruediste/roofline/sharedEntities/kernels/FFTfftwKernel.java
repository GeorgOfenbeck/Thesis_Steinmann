package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

public class FFTfftwKernel extends FFTfftwKernelData {

	@Override
	public String getAdditionalLibraries(SystemInformation systemInformation) {
		return "-lfftw3 -lm";
	}

	@Override
	public String getLabel() {
		return "FFT-FFTW";
	}

}
