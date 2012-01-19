package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

/**
 * represents the bandwith of a kernel
 */
public class Throughput extends Quantity {
	private TransferredBytes transferredBytes;
	private Time time;

	public Throughput(TransferredBytes transferredBytes, Time time) {
		this.transferredBytes = transferredBytes;
		this.time = time;
	}

	public TransferredBytes getTransferredBytes() {
		return transferredBytes;
	}

	public Time getTime() {
		return time;
	}

	@Override
	public double getValue() {
		return transferredBytes.getValue() / time.getValue();
	}
}
