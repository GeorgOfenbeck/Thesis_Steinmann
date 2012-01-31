package ch.ethz.ruediste.roofline.measurementDriver.dom;

import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;

public class RooflinePoint {
	final private String name;
	final private OperationalIntensity operationalIntensity;
	final private Performance performance;

	public RooflinePoint(String name,
			OperationalIntensity operationalIntensity, Performance performance) {
		super();
		this.name = name;
		this.operationalIntensity = operationalIntensity;
		this.performance = performance;
	}

	public String getName() {
		return name;
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
