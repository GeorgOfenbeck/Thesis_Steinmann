package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator;

import java.util.List;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;
import ch.ethz.ruediste.roofline.sharedEntities.*;

public class MultiplyingQuantityCalculator<T extends Quantity<T>> extends
		QuantityCalculator<T> {
	private QuantityCalculator<T> inner;

	public MultiplyingQuantityCalculator(QuantityCalculator<T> inner,
			double factor) {
		super();
		this.inner = inner;
		this.factor = factor;
	}

	private double factor;

	@Override
	public T getSingleResult(Iterable<MeasurerOutputBase> outputs) {
		return inner.getSingleResult(outputs).multiplied(factor);
	}

	@Override
	public List<MeasurerBase> getRequiredMeasurers() {
		return inner.getRequiredMeasurers();
	}

	@Override
	public T getBestResult(Iterable<Iterable<MeasurerOutputBase>> runOutputs) {
		return inner.getBestResult(runOutputs).multiplied(factor);
	}

}
