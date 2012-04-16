package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

public class SubtractingQuantityCalculator<T extends Quantity<T>> extends
		CombiningQuantityCalculator<T, T, T> {

	public SubtractingQuantityCalculator(QuantityCalculator<T> left,
			QuantityCalculator<T> right) {
		super(left, right);
	}

	@Override
	protected T combineResult(T leftResult, T rightResult) {
		return leftResult.subtracted(rightResult);
	}

}
