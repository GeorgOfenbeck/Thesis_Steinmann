package ch.ethz.ruediste.roofline.sharedEntities.actions;

import java.util.Set;

import ch.ethz.ruediste.roofline.sharedEntities.Workload;

public class WaitForWorkloadAction extends WaitForWorkloadActionData {
	private Workload waitForWorkload;

	public WaitForWorkloadAction() {
		super();
	}

	public WaitForWorkloadAction(Workload waitForWorkload) {
		this();
		this.setWaitForWorkload(waitForWorkload);
	}

	@Override
	public int getWaitForWorkloadId() {
		return getWaitForWorkload().getId();
	}

	public Workload getWaitForWorkload() {
		return waitForWorkload;
	}

	public void setWaitForWorkload(Workload waitForWorkload) {
		this.waitForWorkload = waitForWorkload;
	}

	@Override
	public void addAll(Set<Object> result) {
		super.addAll(result);
		if (getWaitForWorkload() != null) {
			getWaitForWorkload().addAll(result);
		}
	}
}