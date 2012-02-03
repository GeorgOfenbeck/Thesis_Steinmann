package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

/**
 * represents the bandwith of a kernel
 */
public class Throughput extends Quantity<Throughput> {

	public Throughput(TransferredBytes transferredBytes, Time time) {
		super(transferredBytes.getValue() / time.getValue());
	}

	private Throughput(double value) {
		super(value);
	}

	@Override
	protected Throughput construct(double value) {
		return new Throughput(value);
	}
}
