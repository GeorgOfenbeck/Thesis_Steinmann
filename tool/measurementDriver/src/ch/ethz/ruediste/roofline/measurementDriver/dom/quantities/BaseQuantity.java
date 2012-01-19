package ch.ethz.ruediste.roofline.measurementDriver.dom.quantities;

public class BaseQuantity extends Quantity {
	double value;

	public BaseQuantity(double value) {
		this.value = value;
	}

	@Override
	public double getValue() {
		return value;
	}
}
