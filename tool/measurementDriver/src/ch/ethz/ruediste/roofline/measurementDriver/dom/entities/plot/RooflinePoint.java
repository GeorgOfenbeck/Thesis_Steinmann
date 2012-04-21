package ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;

public class RooflinePoint {
	final private long problemSize;
	final private OperationalIntensity operationalIntensity;
	final private Performance performance;
	private String label;

	public RooflinePoint(long problemSize,
			OperationalIntensity operationalIntensity, Performance performance) {
		super();
		this.problemSize = problemSize;
		this.operationalIntensity = operationalIntensity;
		this.performance = performance;
	}

	public long getProblemSize() {
		return problemSize;
	}

	public OperationalIntensity getOperationalIntensity() {
		return operationalIntensity;
	}

	public Performance getPerformance() {
		return performance;
	}

	@Override
	public String toString() {
		return String.format("Point: [%s : %s]", getOperationalIntensity(),
				getPerformance());
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
