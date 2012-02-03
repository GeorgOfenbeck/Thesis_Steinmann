package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

public class Time extends Quantity<Time> {

	public Time(double value) {
		super(value);
	}

	@Override
	protected Time construct(double value) {
		return new Time(value);
	}

}
