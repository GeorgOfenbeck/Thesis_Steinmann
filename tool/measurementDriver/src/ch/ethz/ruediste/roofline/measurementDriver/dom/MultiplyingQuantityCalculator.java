package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.Quantity;

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
	public T getResult() {
		return inner.getResult().multiplied(factor);
	}

	@Override
	public ArrayList<MeasurerSet> getRequiredMeasurerSets() {
		return inner.getRequiredMeasurerSets();
	}

	@Override
	public void addOutput(MeasurerSetOutput measurerSetOutput) {
		inner.addOutput(measurerSetOutput);
	}

}
