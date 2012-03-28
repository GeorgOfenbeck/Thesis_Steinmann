package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

public class WhtKernel extends WhtKernelData {

	@Override
	public String getAdditionalLibraries(SystemInformation systemInformation) {
		return "-lwht";
	}

	@Override
	public String getLabel() {
		return "WHT";
	}
}
