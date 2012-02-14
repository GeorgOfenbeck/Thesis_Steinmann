package ch.ethz.ruediste.roofline.dom;

public class FFTmklKernel extends FFTmklKernelData {

	@Override
	public String getAdditionalLibraries() {
		return "-L/opt/intel/mkl/lib/ia32 -lmkl_intel -lmkl_sequential -lmkl_core";
	}

	@Override
	public String getAdditionalIncludeDirs() {
		return "-I/opt/intel/mkl/include";
	}
}
