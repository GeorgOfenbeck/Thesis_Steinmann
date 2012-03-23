package ch.ethz.ruediste.roofline.sharedEntities.kernels;

public class WhtKernel extends WhtKernelData {

	@Override
	public String getAdditionalLibraries() {
		return "-lwht";
	}

	@Override
	public String getLabel() {
		return "WHT";
	}
}
