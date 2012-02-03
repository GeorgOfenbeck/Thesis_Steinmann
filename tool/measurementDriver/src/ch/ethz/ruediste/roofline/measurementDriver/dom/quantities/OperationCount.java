package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

public class OperationCount extends Quantity<OperationCount> {
	public OperationCount(double value) {
		super(value);
	}

	@Override
	protected OperationCount construct(double value) {
		return new OperationCount(value);
	}
}
