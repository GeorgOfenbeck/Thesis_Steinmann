package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import ch.ethz.ruediste.roofline.measurementDriver.dom.*;
import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryAction;

public class HistogramPlot extends Plot<HistogramPlot> implements
		IUnaryAction<Double> {
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

	public void apply(Double v) {
		histogram.apply(v);
	}

}
