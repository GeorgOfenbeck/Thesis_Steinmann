package ch.ethz.ruediste.roofline.sharedEntities.actions;

import java.util.Set;

import ch.ethz.ruediste.roofline.sharedEntities.KernelBase;

public class FlushKernelBuffersAction extends FlushKernelBuffersActionData {
	private KernelBase kernel;

	public FlushKernelBuffersAction() {
	}

	public FlushKernelBuffersAction(KernelBase kernel) {
		this();
		setKernel(kernel);
	}

	@Override
	public int getKernelId() {
		return getKernel().getId();
	}

	public KernelBase getKernel() {
		return kernel;
	}

	public void setKernel(KernelBase kernel) {
		this.kernel = kernel;
	}

	@Override
	public void addAll(Set<Object> result) {
		super.addAll(result);
		if (getKernel() != null) {
			getKernel().addAll(result);
		}
	}
}
