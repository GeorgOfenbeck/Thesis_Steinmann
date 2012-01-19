package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

/**
 * represents the performance of a kernel
 * 
 */
public class Performance extends Quantity {
	private OperationCount operations;
	private Time time;

	public Performance(OperationCount operations, Time time) {
		this.operations = operations;
		this.time = time;
	}

	public OperationCount getOperations() {
		return operations;
	}

	public Time getTime() {
		return time;
	}

	@Override
	public double getValue() {
		return operations.getValue() / time.getValue();
	}
}
