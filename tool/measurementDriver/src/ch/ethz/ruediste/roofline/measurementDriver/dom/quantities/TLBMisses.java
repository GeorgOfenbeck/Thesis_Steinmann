package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

public class TLBMisses extends Quantity<TLBMisses> {

	public TLBMisses(double value) {
		super(value);
	}

	@Override
	protected TLBMisses construct(double value) {
		return new TLBMisses(value);
	}

}
