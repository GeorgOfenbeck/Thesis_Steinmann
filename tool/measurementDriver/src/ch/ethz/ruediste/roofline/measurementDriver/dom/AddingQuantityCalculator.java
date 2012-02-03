package ch.ethz.ruediste.roofline.measurementDriver.dom;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

public class AddingQuantityCalculator<T extends Quantity<T>> extends
		CombiningQuantityCalculator<T> {

	public AddingQuantityCalculator(QuantityCalculator<T> left,
			QuantityCalculator<T> right) {
		super(left, right);
	}

	@Override
	public T getResult() {
		return left.getResult().added(right.getResult());
	}

}
