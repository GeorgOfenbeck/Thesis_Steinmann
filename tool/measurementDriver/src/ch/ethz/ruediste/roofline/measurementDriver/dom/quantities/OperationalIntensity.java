package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

public class OperationalIntensity extends Quantity {
	private final TransferredBytes transferredBytes;
	private final OperationCount operations;

	public OperationalIntensity(TransferredBytes transferredBytes,
			OperationCount operations) {
		this.transferredBytes = transferredBytes;
		this.operations = operations;
	}

	public TransferredBytes getTransferredBytes() {
		return transferredBytes;
	}

	public OperationCount getOperations() {
		return operations;
	}

	@Override
	public double getValue() {
		return operations.getValue() / transferredBytes.getValue();
	}
}
