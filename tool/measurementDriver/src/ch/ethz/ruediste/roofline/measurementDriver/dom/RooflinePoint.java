package ch.ethz.ruediste.roofline.measurementDriver.dom;

public class RooflinePoint {
	private String name;
	private double operations;
	private double transferredBytes;
	private double time;

	public RooflinePoint(String name, double operations,
			double transferredBytes, double time) {
		super();
		this.name = name;
		this.operations = operations;
		this.transferredBytes = transferredBytes;
		this.time = time;
	}

	public double getOperations() {
		return operations;
	}

	public void setOperations(double operations) {
		this.operations = operations;
	}

	public double getTransferredBytes() {
		return transferredBytes;
	}

	public void setTransferredBytes(double transferredBytes) {
		this.transferredBytes = transferredBytes;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getOperationalIntensity() {
		return operations / transferredBytes;
	}

	public double getPerformance() {
		return operations / time;
	}

	@Override
	public String toString() {
		return String.format("Point: [%g : %g]", getOperationalIntensity(),
				getPerformance());
	}
}
