package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import org.apache.commons.lang3.Range;

import ch.ethz.ruediste.roofline.sharedEntities.SystemInformation;

/*
 * A 2D plot, specifies the axis names and units
 */
public abstract class Plot2D<T extends Plot2D<?>> extends Plot<T> {
	private Range<Double> yRange = Range.between(Double.NEGATIVE_INFINITY,
			Double.POSITIVE_INFINITY);
	private Range<Double> xRange = Range.between(Double.NEGATIVE_INFINITY,
			Double.POSITIVE_INFINITY);

	private String xLabel;
	private String xUnit;
	private String yLabel;
	private String yUnit;
	private KeyPosition keyPosition = KeyPosition.TopLeft;

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

	public Range<Double> getYRange(SystemInformation systemInformation) {
		return yRange;
	}

	public Range<Double> getXRange(SystemInformation systemInformation) {
		return xRange;
	}

	public boolean isLogX() {
		return logX;
	}

	public T setLogX(boolean logX) {
		this.logX = logX;
		return This();
	}

	public T setLogX() {
		return setLogX(true);
	}

	public boolean isLogY() {
		return logY;
	}

	public T setLogY(boolean logY) {
		this.logY = logY;
		return This();
	}

	public T setLogY() {
		return setLogY(true);
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

	public KeyPosition getKeyPosition() {
		return keyPosition;
	}

	public T setKeyPosition(KeyPosition keyPosition) {
		this.keyPosition = keyPosition;
		return This();
	}

	public T setYRange(Range<Double> yRange) {
		this.yRange = yRange;
		return This();
	}

	public T setYRange(double min, double max) {
		return setYRange(Range.between(min, max));
	}

	public T setXRange(Range<Double> xRange) {
		this.xRange = xRange;
		return This();
	}

	public T setXRange(double min, double max) {
		return setXRange(Range.between(min, max));
	}

}
