package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

public class ContextSwitches extends Quantity<ContextSwitches> {

	public ContextSwitches(double value) {
		super(value);
	}

	@Override
	protected ContextSwitches construct(double value) {
		return new ContextSwitches(value);
	}

}
