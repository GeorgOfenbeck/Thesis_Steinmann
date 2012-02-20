package ch.ethz.ruediste.roofline.measurementDriver.dom;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;

public class RooflinePoint {
	final private String label;
	final private OperationalIntensity operationalIntensity;
	final private Performance performance;

	public RooflinePoint(String label,
			OperationalIntensity operationalIntensity, Performance performance) {
		super();
		this.label = label;
		this.operationalIntensity = operationalIntensity;
		this.performance = performance;
	}

	public String getLabel() {
		return label;
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
}
