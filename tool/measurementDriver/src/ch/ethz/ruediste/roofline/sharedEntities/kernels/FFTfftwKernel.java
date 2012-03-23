package ch.ethz.ruediste.roofline.sharedEntities.kernels;

public class FFTfftwKernel extends FFTfftwKernelData {

	@Override
	public String getAdditionalLibraries() {
		return "-lfftw3 -lm";
	}

	@Override
	public String getLabel() {
		return "FFT-FFTW";
	}

}
