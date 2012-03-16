package ch.ethz.ruediste.roofline.sharedEntities.kernels;

public class FFTmklKernel extends FFTmklKernelData {

	@Override
	public String getAdditionalLibraries() {
		return LibraryHelper.getMklLibs(false);
	}

	@Override
	public String getAdditionalIncludeDirs() {
		return "-I/opt/intel/mkl/include";
	}
}
