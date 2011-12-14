package ch.ethz.ruediste.roofline.measurementDriver.dom;

import java.util.ArrayList;

public class SimplePlot extends Plot {
	ArrayList<Double> values = new ArrayList<Double>();

	public void addValue(double v) {
		values.add(v);
	}

	public Iterable<Double> getValues() {
		return values;
	}
}
