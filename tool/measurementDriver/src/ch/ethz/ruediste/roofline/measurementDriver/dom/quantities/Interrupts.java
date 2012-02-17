package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

public class Interrupts extends Quantity<Interrupts> {

	public Interrupts(double value) {
		super(value);
	}

	@Override
	protected Interrupts construct(double value) {
		return new Interrupts(value);
	}

}
