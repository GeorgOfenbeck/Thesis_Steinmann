package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.sharedEntities.MeasurerOutputBase;

public class SubtractingQuantityCalculator<T extends Quantity<T>> extends
		CombiningQuantityCalculator<T> {

	public SubtractingQuantityCalculator(QuantityCalculator<T> left,
			QuantityCalculator<T> right) {
		super(left, right);
	}

	@Override
	protected T combineResults(ArrayList<MeasurerOutputBase> leftOutputs,
			ArrayList<MeasurerOutputBase> rightOutputs) {
		return left.getResult(leftOutputs).subtracted(
				right.getResult(rightOutputs));
	}

}