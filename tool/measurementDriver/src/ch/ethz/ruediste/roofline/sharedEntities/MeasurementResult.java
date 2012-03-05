package ch.ethz.ruediste.roofline.sharedEntities;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.util.*;

public class MeasurementResult {
	private Measurement measurement;
	private final List<MeasurementRunOutput> runOutputs = new ArrayList<MeasurementRunOutput>();
	private CoreHash coreHash;

	/**
	 * return the outputs of the measurement runs
	 */
	public List<MeasurementRunOutput> getRunOutputs() {
		return runOutputs;
	}

	/**
	 * return the measurement
	 */
	public Measurement getMeasurement() {
		return measurement;
	}

	/**
	 * set the measurement
	 */
	public void setMeasurement(Measurement measurement) {
		this.measurement = measurement;
	}

	/**
	 * add all run outputs in a run output collection to the result
	 */
	public void add(MeasurementRunOutputCollection outputCollection) {
		getRunOutputs().addAll(outputCollection.getOutputs());
	}

	/**
	 * get the hash of the measuring core used to generate the result
	 */
	public CoreHash getCoreHash() {
		return coreHash;
	}

	/**
	 * set the hash of the measuring core used to generate the result
	 */
	public void setCoreHash(CoreHash coreHash) {
		this.coreHash = coreHash;
	}

	/**
	 * return the results of a measurer, one for each run
	 */
	public <TOutput> Iterable<TOutput> getMeasurerOutputs(
			IMeasurer<TOutput> measurer) {
		List<TOutput> result = new ArrayList<TOutput>();
		for (MeasurementRunOutput output : getRunOutputs()) {
			result.add(output.getMeasurerOutput(measurer));
		}
		return result;
	}

	/**
	 * return the results of a measurer, one for each run
	 */
	public Iterable<MeasurerOutputBase> getMeasurerOutputsUntyped(
			MeasurerBase measurer) {
		List<MeasurerOutputBase> result = new ArrayList<MeasurerOutputBase>();
		for (MeasurementRunOutput output : getRunOutputs()) {
			result.add(output.getMeasurerOutputUntyped(measurer));
		}
		return result;
	}

	/**
	 * return the outputs of a measurer set, one for each run
	 */
	public Iterable<MeasurerSetOutput> getMeasurerSetOutputsUntyped(
			final MeasurerSet measurerSet) {
		return IterableUtils.select(getRunOutputs(),
				new IUnaryFunction<MeasurementRunOutput, MeasurerSetOutput>() {
					public MeasurerSetOutput apply(MeasurementRunOutput arg) {
						return arg.getMeasurerSetOutputUntyped(measurerSet);
					}
				});
	}

	/**
	 * sets the UIDs of the result based on the UIDs of the measurement
	 */
	public void setResultUids() {
		Measurement measurement = getMeasurement();

		// iterate over all runs
		for (MeasurementRunOutput runOutput : getRunOutputs()) {
			// iterate over all measurer set outputs
			for (MeasurerSetOutput setOutput : runOutput
					.getMeasurerSetOutputs()) {

				// set the UID of the set output
				setOutput.setSetUid(measurement.getMeasurerSet(
						setOutput.getSetId()).getUid());

				// iterate over the measurers in the set
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
