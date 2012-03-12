package ch.ethz.ruediste.roofline.sharedEntities.actions;

import ch.ethz.ruediste.roofline.sharedEntities.KernelBase;

public class StopKernelAction extends StopKernelActionData {

	public StopKernelAction() {

	}

	public StopKernelAction(KernelBase kernel) {
		this();
		setKernel(kernel);
	}
}
