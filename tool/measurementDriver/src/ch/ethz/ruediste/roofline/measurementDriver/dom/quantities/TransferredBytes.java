package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

public class TransferredBytes extends Quantity<TransferredBytes> {

	public TransferredBytes(double value) {
		super(value);
	}

	@Override
	protected TransferredBytes construct(double value) {
		return new TransferredBytes(value);
	}

}
