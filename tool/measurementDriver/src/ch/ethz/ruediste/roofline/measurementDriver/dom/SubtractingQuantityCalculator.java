package ch.ethz.ruediste.roofline.measurementDriver.dom;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

public class SubtractingQuantityCalculator<T extends Quantity<T>> extends
		CombiningQuantityCalculator<T> {

	public SubtractingQuantityCalculator(QuantityCalculator<T> left,
			QuantityCalculator<T> right) {
		super(left, right);
	}

	@Override
	public T getResult() {
		return left.getResult().subtracted(right.getResult());
	}

}
