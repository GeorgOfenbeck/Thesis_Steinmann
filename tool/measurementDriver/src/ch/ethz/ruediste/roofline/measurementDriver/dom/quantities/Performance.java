package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

/**
 * represents the performance of a kernel
 * 
 */
public class Performance extends Quantity<Performance> {
	public Performance(OperationCount operations, Time time) {
		super(operations.getValue() / time.getValue());
	}

	private Performance(double value) {
		super(value);
	}

	@Override
	protected Performance construct(double value) {
		return new Performance(value);
	}

}
