package ch.ethz.ruediste.roofline.dom;

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
}