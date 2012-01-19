package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

public abstract class Quantity {
	public abstract double getValue();

	@Override
	public String toString() {
		return Double.toString(getValue());
	}
}
