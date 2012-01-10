package ch.ethz.ruediste.roofline.measurementDriver.dom;

/**
 * represents the bandwith of a kernel
 */
public class Bandwidth {
	private String name;
	private double transferredBytes;
	private double time;

	public Bandwidth(String name, double transferredBytes, double time) {
		super();
		this.name = name;
		this.transferredBytes = transferredBytes;
		this.time = time;
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

	public double getBandwidth() {
		return transferredBytes / time;
	}
}
