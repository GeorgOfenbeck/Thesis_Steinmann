package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

public class OperationalIntensity extends Quantity<OperationalIntensity> {

	public OperationalIntensity(TransferredBytes transferredBytes,
			OperationCount operations) {
		super(operations.getValue() / transferredBytes.getValue());
	}

	private OperationalIntensity(double d) {
		super(d);
	}

	@Override
	protected OperationalIntensity construct(double value) {
		return new OperationalIntensity(value);
	}

}
