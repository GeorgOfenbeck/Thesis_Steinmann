package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;

import ch.ethz.ruediste.roofline.statistics.IAddValue;

public class SimplePlot extends Plot implements IAddValue {
	ArrayList<Double> values = new ArrayList<Double>();

	public void addValue(double v) {
		values.add(v);
	}

	public Iterable<Double> getValues() {
		return values;
	}
}
