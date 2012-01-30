package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryAction;

public class SimplePlot extends Plot implements IUnaryAction<Double> {
	ArrayList<Double> values = new ArrayList<Double>();

	public void apply(Double v) {
		values.add(v);
	}

	public Iterable<Double> getValues() {
		return values;
	}
}
