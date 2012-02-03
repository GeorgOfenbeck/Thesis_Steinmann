package ch.ethz.ruediste.roofline.statistics;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class UncertainValue {

	private double value;
	private double stdev;
	private double min;
	private double max;

	public UncertainValue(double value, double stdev, double min, double max) {
		super();
		this.value = value;
		this.stdev = stdev;
		this.min = min;
		this.max = max;
	}

	public UncertainValue(DescriptiveStatistics summary) {
		value = summary.getMean();
		stdev = summary.getStandardDeviation();
		min = summary.getMin();
		max = summary.getMax();
	}

	public UncertainValue(double value) {
		this.value = value;
		stdev = 0;
		min = value;
		max = value;
	}

	public double getValue() {
		return value;
	}

	public double getStdev() {
		return stdev;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public UncertainValue Minus(UncertainValue other) {
		double min = Math.min(
				Math.min(this.min - other.min, this.max - other.min),
				Math.min(this.min - other.max, this.max - other.max));
		double max = Math.max(
				Math.max(this.min - other.min, this.max - other.min),
				Math.max(this.min - other.max, this.max - other.max));
		return new UncertainValue(value - other.value, SumDev(other), min, max);
	}

	public UncertainValue Plus(UncertainValue other) {
		double min = Math.min(
				Math.min(this.min + other.min, this.max + other.min),
				Math.min(this.min + other.max, this.max + other.max));
		double max = Math.max(
				Math.max(this.min + other.min, this.max + other.min),
				Math.max(this.min + other.max, this.max + other.max));
		return new UncertainValue(value + other.value, SumDev(other), min, max);
	}

	private double SumDev(UncertainValue other) {
		return Math.sqrt(Math.pow(value * stdev, 2)
				+ Math.pow(other.value * other.stdev, 2));
	}

	public UncertainValue Multiply(UncertainValue other) {
		double min = Math.min(
				Math.min(this.min * other.min, this.max * other.min),
				Math.min(this.min * other.max, this.max * other.max));
		double max = Math.max(
				Math.max(this.min * other.min, this.max * other.min),
				Math.max(this.min * other.max, this.max * other.max));
		double newValue = value * other.value;
		return new UncertainValue(newValue, MulDev(newValue, other), min, max);
	}

	public UncertainValue Divide(UncertainValue other) {
		double min = Math.min(
				Math.min(this.min / other.min, this.max / other.min),
				Math.min(this.min / other.max, this.max / other.max));
		double max = Math.max(
				Math.max(this.min / other.min, this.max / other.min),
				Math.max(this.min / other.max, this.max / other.max));
		double newValue = value / other.value;
		return new UncertainValue(newValue, MulDev(newValue, other), min, max);
	}

	private double MulDev(double newValue, UncertainValue other) {
		return Math.abs(newValue)
				* Math.sqrt(Math.pow(stdev / value, 2)
						+ Math.pow(other.stdev / other.value, 2));
	}
}
