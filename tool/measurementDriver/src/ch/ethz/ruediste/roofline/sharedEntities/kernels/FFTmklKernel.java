package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.*;

public class FFTmklKernel extends FFTmklKernelData {

	@Override
	public String getAdditionalLibraries(SystemInformation systemInformation) {
		return LibraryHelper.getMklLibs(getNumThreads() > 1, systemInformation);
	}

	@Override
	public String getAdditionalIncludeDirs() {
		return "-I/opt/intel/mkl/include";
	}

	@Override
	public Operation getSuggestedOperation() {
		return Operation.DoublePrecisionFlop;
	}

	@Override
	public String getLabelOverride() {
		return "FFT-MKL";
	}

	@Override
	public long getDataSize() {
		return getBufferSize() * 16;
	}
}
