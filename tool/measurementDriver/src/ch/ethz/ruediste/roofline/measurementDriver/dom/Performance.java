package ch.ethz.ruediste.roofline.measurementDriver.dom;

/**
 * represents the performance of a kernel
 * 
 */
public class Performance {
	private String name;
	private double operations;
	private double time;

	public Performance(String name, double operations, double time) {
		super();
		this.name = name;
		this.operations = operations;
		this.time = time;
	}

	public double getOperations() {
		return operations;
	}

	public void setOperations(double operations) {
		this.operations = operations;
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

	public double getPerformance() {
		return operations / time;
	}
}
