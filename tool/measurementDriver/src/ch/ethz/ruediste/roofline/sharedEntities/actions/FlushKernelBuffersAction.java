package ch.ethz.ruediste.roofline.sharedEntities.actions;

import ch.ethz.ruediste.roofline.sharedEntities.KernelBase;

public class FlushKernelBuffersAction extends FlushKernelBuffersActionData {

	public FlushKernelBuffersAction() {
	}

	public FlushKernelBuffersAction(KernelBase kernel) {
		this();
		setKernel(kernel);
	}
}
