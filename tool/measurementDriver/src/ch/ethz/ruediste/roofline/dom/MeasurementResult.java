package ch.ethz.ruediste.roofline.dom;

import java.util.*;

public class MeasurementResult {
	private MeasurementDescription measurement;
	private final List<MeasurementRunOutput> outputs = new ArrayList<MeasurementRunOutput>();
	private CoreHash coreHash;

	public List<MeasurementRunOutput> getOutputs() {
		return outputs;
	}

	public MeasurementDescription getMeasurement() {
		return measurement;
	}

	public void setMeasurement(MeasurementDescription measurement) {
		this.measurement = measurement;
	}

	public void add(MeasurementRunOutputCollection outputCollection) {
		getOutputs().addAll(outputCollection.getOutputs());
	}

	public CoreHash getCoreHash() {
		return coreHash;
	}

	public void setCoreHash(CoreHash coreHash) {
		this.coreHash = coreHash;
	}

	public <TOutput> Iterable<TOutput> getMeasurerOutputs(
			IMeasurerDescription<TOutput> measurer) {
		List<TOutput> result = new ArrayList<TOutput>();
		for (MeasurementRunOutput output : getOutputs()) {

			result.add(output.getMeasurerOutput(measurer));
		}
		return result;
	}

}
