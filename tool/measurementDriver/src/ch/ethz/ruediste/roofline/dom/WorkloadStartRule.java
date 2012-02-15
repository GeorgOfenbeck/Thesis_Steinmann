package ch.ethz.ruediste.roofline.dom;

public class WorkloadStartRule extends WorkloadStartRuleData {

	private Workload workload;

	public WorkloadStartRule() {
		super();
	}

	public WorkloadStartRule(Workload workload) {
		this();
		this.workload = workload;
	}

	public Workload getWorkload() {
		return workload;
	}

	public void setWorkload(Workload workload) {
		this.workload = workload;
	}

	@Override
	public int getWorkloadId() {
		return workload.getId();
	}
}
