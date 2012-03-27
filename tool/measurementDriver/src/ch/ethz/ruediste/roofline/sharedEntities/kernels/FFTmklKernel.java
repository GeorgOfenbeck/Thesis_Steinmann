package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.Operation;

public class FFTmklKernel extends FFTmklKernelData {

	@Override
	public String getAdditionalLibraries() {
		return LibraryHelper.getMklLibs(false);
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
	public String getLabel() {
		return "FFT-MKL";
	}

	@Override
	public long getDataSize() {
		return getBufferSize() * 16;
	}
}
