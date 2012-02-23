package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.*;

import ch.ethz.ruediste.roofline.dom.MeasurerBase;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

public abstract class TerminalQuantityCalculator<T extends Quantity<T>> extends
		QuantityCalculator<T> {

	protected final MeasurerBase requiredMeasurer;

	public TerminalQuantityCalculator(MeasurerBase requiredMeasurer) {
		this.requiredMeasurer = requiredMeasurer;

	}

	@Override
	public List<MeasurerBase> getRequiredMeasurers() {
		return Collections.<MeasurerBase> singletonList(requiredMeasurer);
	}
}
