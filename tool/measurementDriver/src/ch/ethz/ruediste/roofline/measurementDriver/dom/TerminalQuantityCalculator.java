package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.*;

import ch.ethz.ruediste.roofline.dom.MeasurerSet;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

public abstract class TerminalQuantityCalculator<T extends Quantity<T>> extends
		QuantityCalculator<T> {

	protected final MeasurerSet<?> requiredMeasurerSet;

	public TerminalQuantityCalculator(MeasurerSet<?> requiredMeasurerSet) {
		this.requiredMeasurerSet = requiredMeasurerSet;

	}

	@Override
	public List<MeasurerSet<?>> getRequiredMeasurerSets() {
		return Collections.<MeasurerSet<?>> singletonList(requiredMeasurerSet);
	}
}
