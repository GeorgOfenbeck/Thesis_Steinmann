package ch.ethz.ruediste.roofline.sharedEntities.actions;

import ch.ethz.ruediste.roofline.sharedEntities.Workload;

public class WaitForWorkloadAction extends WaitForWorkloadActionData {

	public WaitForWorkloadAction() {
		super();
	}

	public WaitForWorkloadAction(Workload waitForWorkload) {
		this();
		setWaitForWorkload(waitForWorkload);
	}
}