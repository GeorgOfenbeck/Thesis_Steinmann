package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;

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
	public void addOutput(final MeasurerSetOutput measurerSetOutput) {
		// predicate indicating a measurer set was used to generate the given output
		IUnaryPredicate<MeasurerSet> predicate = new IUnaryPredicate<MeasurerSet>() {
			public Boolean apply(MeasurerSet arg) {
				return arg.getId() == measurerSetOutput.getSetId();
			}
		};

		// check if the output belongs to the left calculator
		if (IterableUtils.any(left.getRequiredMeasurerSets(), predicate)) {
			left.addOutput(measurerSetOutput);
		}
		else {
			if (!IterableUtils.any(right.getRequiredMeasurerSets(), predicate)) {
				throw new Error(
						"no matching measurer set found for the supplied output");
			}
			right.addOutput(measurerSetOutput);
		}
	}
}
