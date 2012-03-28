package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

public class HistogramPlot extends Plot2D<HistogramPlot> {
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

	public void addValue(Double v) {
		histogram.apply(v);
	}

}
