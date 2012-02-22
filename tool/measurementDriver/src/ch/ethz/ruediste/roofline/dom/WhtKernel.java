package ch.ethz.ruediste.roofline.dom;

public class WhtKernel extends WhtKernelData {

	@Override
	public String getAdditionalLibraries() {
		return "-lwht";
	}
}
