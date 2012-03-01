package ch.ethz.ruediste.roofline.sharedEntities;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.util.*;
import ch.ethz.ruediste.roofline.sharedEntities.*;
import ch.ethz.ruediste.roofline.sharedEntities.measurers.*;

public class MeasurementResult {
	private Measurement measurement;
	private final List<MeasurementRunOutput> runOutputs = new ArrayList<MeasurementRunOutput>();
	private CoreHash coreHash;

	public List<MeasurementRunOutput> getRunOutputs() {
		return runOutputs;
	}

	public Measurement getMeasurement() {
		return measurement;
	}

	public void setMeasurement(Measurement measurement) {
		this.measurement = measurement;
	}

	public void add(MeasurementRunOutputCollection outputCollection) {
		getRunOutputs().addAll(outputCollection.getOutputs());
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
		for (MeasurementRunOutput output : getRunOutputs()) {
			result.add(output.getMeasurerOutput(measurer));
		}
		return result;
	}

	public Iterable<MeasurerOutputBase> getMeasurerOutputsUntyped(MeasurerBase measurer) {
		List<MeasurerOutputBase> result = new ArrayList<MeasurerOutputBase>();
		for (MeasurementRunOutput output : getRunOutputs()) {
			result.add(output.getMeasurerOutput(measurer));
		}
		return result;
	}

	public Iterable<MeasurerSetOutput> getMeasurerSetOutputsUntyped(
			final MeasurerSet measurerSet) {
		return IterableUtils.select(getRunOutputs(),
				new IUnaryFunction<MeasurementRunOutput, MeasurerSetOutput>() {
					public MeasurerSetOutput apply(MeasurementRunOutput arg) {
						return arg.getMeasurerSetOutput(measurerSet);
					}
				});
	}

	public void setResultUids() {
		Measurement measurement = getMeasurement();

		for (MeasurementRunOutput runOutput : getRunOutputs()) {
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
