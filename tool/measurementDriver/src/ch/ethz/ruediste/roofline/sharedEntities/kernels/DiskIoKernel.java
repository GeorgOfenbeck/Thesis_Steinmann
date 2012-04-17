package ch.ethz.ruediste.roofline.sharedEntities.kernels;

public class DiskIoKernel extends DiskIoKernelData {

	@Override
	public String getLabelOverride() {
		return "DiskIo";
	}

}
