package ch.ethz.ruediste.roofline.dom;

public class FFTfftwKernel extends FFTfftwKernelData {

	@Override
	public String getAdditionalLibraries() {
		return "-lfftw3 -lm";
	}
}
