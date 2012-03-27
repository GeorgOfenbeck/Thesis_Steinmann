package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.sharedEntities.MeasurerOutputBase;

public class DividingQuantityCalculator<T extends Quantity<T>, TLeft extends Quantity<TLeft>, TRight extends Quantity<TRight>>
		extends
		CombiningQuantityCalculator<T, TLeft, TRight> {

	private final Class<T> clazz;

	public DividingQuantityCalculator(Class<T> clazz,
			QuantityCalculator<TLeft> left,
			QuantityCalculator<TRight> right) {
		super(left, right);
		this.clazz = clazz;
	}

	@Override
	protected T combineResults(ArrayList<MeasurerOutputBase> leftOutputs,
			ArrayList<MeasurerOutputBase> rightOutputs) {
		return Quantity.construct(clazz, left.getResult(leftOutputs).getValue()
				/ right.getResult(rightOutputs).getValue());
	}

	public static <T extends Quantity<T>, TLeft extends Quantity<TLeft>, TRight extends Quantity<TRight>>
			DividingQuantityCalculator<T, TLeft, TRight> create(Class<T> clazz,
					QuantityCalculator<TLeft> left,
					QuantityCalculator<TRight> right) {
		return new DividingQuantityCalculator<T, TLeft, TRight>(clazz, left,
				right);
	}
}
