package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.dom.MeasurerSetOutput;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

public class AddingQuantityCalculator<T extends Quantity<T>> extends
		CombiningQuantityCalculator<T> {

	public AddingQuantityCalculator(QuantityCalculator<T> left,
			QuantityCalculator<T> right) {
		super(left, right);
	}

	@Override
	protected T combineResults(ArrayList<MeasurerSetOutput> leftOutputs,
			ArrayList<MeasurerSetOutput> rightOutputs) {
		return left.getResult(leftOutputs).added(right.getResult(rightOutputs));
	}

}
