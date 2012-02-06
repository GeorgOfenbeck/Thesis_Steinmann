package ch.ethz.ruediste.roofline.dom;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.util.*;

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

	public <TOutput, TMeasurer extends MeasurerDescriptionBase & IMeasurerDescription<TOutput>> Iterable<MeasurerSetOutput> getMeasurerSetOutputs(
			final MeasurerSet<TMeasurer> measurerSet) {
		return IterableUtils.select(getOutputs(),
				new IUnaryFunction<MeasurementRunOutput, MeasurerSetOutput>() {
					public MeasurerSetOutput apply(MeasurementRunOutput arg) {
						return arg.getMeasurerSetOutput(measurerSet);
					}
				});
	}

}
