package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

public class AddingQuantityCalculator<T extends Quantity<T>> extends
		CombiningQuantityCalculator<T, T, T> {

	public AddingQuantityCalculator(QuantityCalculator<T> left,
			QuantityCalculator<T> right) {
		super(left, right);
	}

	@Override
	protected T combineResult(T leftResult, T rightResult) {
		return leftResult.added(rightResult);
	}
}
