package ch.ethz.ruediste.roofline.dom;

import java.util.ArrayList;
import java.util.List;

public class MeasurementResult {
	private MeasurementDescription measurement;
	private List<MeasurerOutputBase> outputs = new ArrayList<MeasurerOutputBase>();

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

}
