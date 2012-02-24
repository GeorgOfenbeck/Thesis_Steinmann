package ch.ethz.ruediste.roofline.sharedEntities.actions;

import ch.ethz.ruediste.roofline.sharedEntities.KernelBase;

public class StopKernelAction extends StopKernelActionData {
	private KernelBase kernel;

	public StopKernelAction() {

	}

	public StopKernelAction(KernelBase kernel) {
		this.kernel = kernel;
	}

	@Override
	public int getKernelId() {
		return kernel.getId();
	}

	public KernelBase getKernel() {
		return kernel;
	}

	public void setKernel(KernelBase kernel) {
		this.kernel = kernel;
	}

}
