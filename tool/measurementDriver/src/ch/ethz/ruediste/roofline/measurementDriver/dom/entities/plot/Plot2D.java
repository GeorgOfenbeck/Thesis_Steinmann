package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import org.apache.commons.lang3.Range;

/*
 * A 2D plot, specifies the axis names and units
 */
public abstract class Plot2D<T extends Plot2D<?>> extends Plot<T> {
	private String xLabel;
	private String xUnit;
	private String yLabel;
	private String yUnit;

	private boolean logX;
	private boolean logY;

	public String getxLabel() {
		return xLabel;
	}

	public T setxLabel(String xLabel) {
		this.xLabel = xLabel;
		return This();
	}

	public String getxUnit() {
		return xUnit;
	}

	public T setxUnit(String xUnit) {
		this.xUnit = xUnit;
		return This();
	}

	public String getyLabel() {
		return yLabel;
	}

	public T setyLabel(String yLabel) {
		this.yLabel = yLabel;
		return This();
	}

	public String getyUnit() {
		return yUnit;
	}

	public T setyUnit(String yUnit) {
		this.yUnit = yUnit;
		return This();
	}

	public Range<Double> getYRange() {
		return Range
				.between(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public Range<Double> getXRange() {
		return Range
				.between(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public boolean isLogX() {
		return logX;
	}

	public T setLogX(boolean logX) {
		this.logX = logX;
		return This();
	}

	public boolean isLogY() {
		return logY;
	}

	public T setLogY(boolean logY) {
		this.logY = logY;
		return This();
	}

	/**
	 * sets logX and logY to true
	 * 
	 * @return
	 */
	public T setLog() {
		setLogX(true);
		setLogY(true);
		return This();
	}
}