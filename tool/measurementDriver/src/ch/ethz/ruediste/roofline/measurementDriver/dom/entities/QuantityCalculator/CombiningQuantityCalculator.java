package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;

public abstract class CombiningQuantityCalculator<T extends Quantity<T>, TLeft extends Quantity<TLeft>, TRight extends Quantity<TRight>>
		extends QuantityCalculator<T> {

	protected final QuantityCalculator<TLeft> left;
	protected final QuantityCalculator<TRight> right;

	public CombiningQuantityCalculator(QuantityCalculator<TLeft> left,
			QuantityCalculator<TRight> right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public ArrayList<MeasurerBase> getRequiredMeasurers() {
		ArrayList<MeasurerBase> result = new ArrayList<MeasurerBase>();
		result.addAll(left.getRequiredMeasurers());
		result.addAll(right.getRequiredMeasurers());
		return result;
	}

	@Override
	final public T getResult(Iterable<MeasurerOutputBase> outputs) {
		ArrayList<MeasurerOutputBase> leftOutputs = new ArrayList<MeasurerOutputBase>();
		ArrayList<MeasurerOutputBase> rightOutputs = new ArrayList<MeasurerOutputBase>();

		// assing all provided outputs to the left or the right calculator
		for (final MeasurerOutputBase output : outputs) {
			// predicate indicating a measurer was used to generate the current output
			IUnaryPredicate<MeasurerBase> predicate = new IUnaryPredicate<MeasurerBase>() {
				public Boolean apply(MeasurerBase arg) {
					return output.isFrom(arg);
				}
			};

			// check if the output belongs to the left calculator
			if (IterableUtils.any(left.getRequiredMeasurers(), predicate)) {
				leftOutputs.add(output);
			}
			else {
				if (!IterableUtils.any(right.getRequiredMeasurers(), predicate)) {
					throw new Error(
							"no matching measurer set found for the supplied output");
				}
				rightOutputs.add(output);
			}
		}

		// combine the results of the two calculators
		return combineResults(leftOutputs, rightOutputs);
	}

	protected abstract T combineResults(
			ArrayList<MeasurerOutputBase> leftOutputs,
			ArrayList<MeasurerOutputBase> rightOutputs);

}
