package ch.ethz.ruediste.roofline.dom;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.*;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.util.IUnaryPredicate;

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
		Iterable<TOutput> result = getMeasurerOutputsUnvalidated(measurer);
		for (TOutput output : result) {
			measurer.validate(output, this);
		}
		return result;
	}

	public <TOutput> Iterable<TOutput> getMeasurerOutputsUnvalidated(
			IMeasurerDescription<TOutput> measurer) {
		List<TOutput> result = new ArrayList<TOutput>();
		for (MeasurementRunOutput output : getOutputs()) {
			result.add(getMeasurerOutput(output, measurer));
		}
		return result;
	}

	public <TOutput> TOutput getMeasurerOutput(MeasurementRunOutput runOutput,
			final IMeasurerDescription<TOutput> measurer) {
		TOutput result = getMeasurerOutputUnvalidated(runOutput, measurer);
		measurer.validate(result, this);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <TOutput> TOutput getMeasurerOutputUnvalidated(
			MeasurementRunOutput runOutput,
			final IMeasurerDescription<TOutput> measurer) {

		// setup predicate
		IUnaryPredicate<MeasurerDescriptionBase> match = new IUnaryPredicate<MeasurerDescriptionBase>() {
			public Boolean apply(MeasurerDescriptionBase arg) {
				return arg == measurer;
			}
		};

		// is the output of the main measurer desired?
		if (match.apply(getMeasurement().getMeasurer())) {
			// check if none of the additional measurer matches
			if (any(getMeasurement().getAdditionalMeasurers(), match)) {
				throw new Error("Multiple matches");
			}

			// return the output of the main measurer;
			return (TOutput) runOutput.getMainMeasurerOutput();
		}
		// is it a validation measurer?
		else if (any(getMeasurement().getValidationMeasurers(), match)) {
			// get the index of the measurer
			int index = indexOfSingle(
					getMeasurement().getValidationMeasurers(), match);

			// return the output of the measurer at the same position
			return (TOutput) runOutput.getValidationMeasurerOutputs()
					.get(index);
		}
		// it must be an additional measurer!
		else {
			// get the index of the measurer
			int index = indexOfSingle(
					getMeasurement().getAdditionalMeasurers(), match);

			// return the output of the measurer at the same position
			return (TOutput) runOutput.getAdditionalMeasurerOutputs()
					.get(index);
		}
	}
}
