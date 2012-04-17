package ch.ethz.ruediste.roofline.sharedEntities.kernels;

public class CpuMigratingKernel extends CpuMigratingKernelData {

	@Override
	public String getLabelOverride() {
		return "CpuMigratingKernel";
	}

}
