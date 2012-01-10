package ch.ethz.ruediste.roofline.measurementDriver.dom;

import ch.ethz.ruediste.roofline.statistics.IAddValue;

public class HistogramPlot extends Plot implements IAddValue {
	private final Histogram histogram;

	public HistogramPlot() {
		this.histogram = new Histogram();
	}

	public HistogramPlot(Histogram histogram) {
		this.histogram = histogram;
	}

	public Histogram getHistogram() {
		return histogram;
	}

	public void addValue(double v) {
		histogram.addValue(v);
	}

}
