package ch.ethz.ruediste.roofline.dom;

import java.util.*;

public class MeasurementResult {
	private MeasurementDescription measurement;
	private final List<MeasurerOutputBase> outputs = new ArrayList<MeasurerOutputBase>();
	private CoreHash coreHash;

	public List<MeasurerOutputBase> getOutputs() {
		return outputs;
	}

	public MeasurementDescription getMeasurement() {
		return measurement;
	}

	public void setMeasurement(MeasurementDescription measurement) {
		this.measurement = measurement;
	}

	public void add(MeasurerOutputCollection outputCollection) {
		getOutputs().addAll(outputCollection.getMeasurerOutputs());
	}

	public CoreHash getCoreHash() {
		return coreHash;
	}

	public void setCoreHash(CoreHash coreHash) {
		this.coreHash = coreHash;
	}

}
