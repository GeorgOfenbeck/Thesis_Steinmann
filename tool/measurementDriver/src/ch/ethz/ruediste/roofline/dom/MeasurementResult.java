package ch.ethz.ruediste.roofline.dom;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.util.*;

public class MeasurementResult {
	private Measurement measurement;
	private final List<MeasurementRunOutput> outputs = new ArrayList<MeasurementRunOutput>();
	private CoreHash coreHash;

	public List<MeasurementRunOutput> getOutputs() {
		return outputs;
	}

	public Measurement getMeasurement() {
		return measurement;
	}

	public void setMeasurement(Measurement measurement) {
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
			IMeasurer<TOutput> measurer) {
		List<TOutput> result = new ArrayList<TOutput>();
		for (MeasurementRunOutput output : getOutputs()) {
			result.add(output.getMeasurerOutput(measurer));
		}
		return result;
	}

	public Iterable<MeasurerSetOutput> getMeasurerSetOutputs(
			final MeasurerSet measurerSet) {
		return IterableUtils.select(getOutputs(),
				new IUnaryFunction<MeasurementRunOutput, MeasurerSetOutput>() {
					public MeasurerSetOutput apply(MeasurementRunOutput arg) {
						return arg.getMeasurerSetOutput(measurerSet);
					}
				});
	}

	public void setResultUids() {
		Measurement measurement = getMeasurement();

		for (MeasurementRunOutput runOutput : getOutputs()) {
			for (MeasurerSetOutput setOutput : runOutput
					.getMeasurerSetOutputs()) {
				// set the UID of the set output
				setOutput.setSetUid(measurement.getMeasurerSet(
						setOutput.getSetId()).getUid());
				for (MeasurerOutputBase measurerOutput : setOutput
						.getMeasurerOutputs()) {

					// set the UID of the measurer output
					{
						if (measurerOutput == null) {
							throw new Error("null measurer output!!!");
						}
						MeasurerBase measurer = measurement
								.getMeasurer(measurerOutput.getMeasurerId());
						measurerOutput.setMeasurerUid(measurer.getUid());
					}
				}
			}
		}
	}

}
