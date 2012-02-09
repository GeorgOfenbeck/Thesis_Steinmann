package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.dom.MeasurerSetOutput;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

public class SubtractingQuantityCalculator<T extends Quantity<T>> extends
		CombiningQuantityCalculator<T> {

	public SubtractingQuantityCalculator(QuantityCalculator<T> left,
			QuantityCalculator<T> right) {
		super(left, right);
	}

	@Override
	protected T combineResults(ArrayList<MeasurerSetOutput> leftOutputs,
			ArrayList<MeasurerSetOutput> rightOutputs) {
		return left.getResult(leftOutputs).subtracted(
				right.getResult(rightOutputs));
	}

}