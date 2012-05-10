package ch.ethz.ruediste.roofline.sharedEntities.kernels;

import ch.ethz.ruediste.roofline.sharedEntities.*;

public class WhtKernel extends WhtKernelData {

	@Override
	public String getAdditionalLibraries(SystemInformation systemInformation) {
		return "-lwht";
	}

	@Override
	public String getLabelOverride() {
		return "WHT";
	}

	@Override
	public Operation getSuggestedOperation() {
		return Operation.CompInstr;
	}
}
