package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.*;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.measurementDriver.util.*;

public abstract class CombiningQuantityCalculator<T extends Quantity<T>>
		extends QuantityCalculator<T> {

	protected final QuantityCalculator<T> left;
	protected final QuantityCalculator<T> right;

	public CombiningQuantityCalculator(QuantityCalculator<T> left,
			QuantityCalculator<T> right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public ArrayList<MeasurerSet> getRequiredMeasurerSets() {
		ArrayList<MeasurerSet> result = new ArrayList<MeasurerSet>();
		result.addAll(left.getRequiredMeasurerSets());
		result.addAll(right.getRequiredMeasurerSets());
		return result;
	}

	@Override
	final public T getResult(List<MeasurerSetOutput> outputs) {
		ArrayList<MeasurerSetOutput> leftOutputs = new ArrayList<MeasurerSetOutput>();
		ArrayList<MeasurerSetOutput> rightOutputs = new ArrayList<MeasurerSetOutput>();

		// assing all provided outputs to the left or the right calculator
		for (final MeasurerSetOutput output : outputs) {
			// predicate indicating a measurer set was used to generate the current output
			IUnaryPredicate<MeasurerSet> predicate = new IUnaryPredicate<MeasurerSet>() {
				public Boolean apply(MeasurerSet arg) {
					return arg.getId() == output.getSetId();
				}
			};

			// check if the output belongs to the left calculator
			if (IterableUtils.any(left.getRequiredMeasurerSets(), predicate)) {
				leftOutputs.add(output);
			}
			else {
				if (!IterableUtils.any(right.getRequiredMeasurerSets(),
						predicate)) {
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
			ArrayList<MeasurerSetOutput> leftOutputs,
			ArrayList<MeasurerSetOutput> rightOutputs);

}
