package ch.ethz.ruediste.roofline.dom;

public class WorkloadStopRule extends WorkloadStopRuleData {
	private Workload workload;

	@Override
	public int getWorkloadId() {
		return workload.getId();
	}

	public Workload getWorkload() {
		return workload;
	}

	public void setWorkload(Workload workload) {
		this.workload = workload;
	}
}
