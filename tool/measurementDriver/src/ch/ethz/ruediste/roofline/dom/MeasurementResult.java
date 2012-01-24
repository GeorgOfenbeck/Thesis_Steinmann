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
		List<TOutput> result = new ArrayList<TOutput>();
		for (MeasurementRunOutput output : getOutputs()) {
			result.add(getMeasurerOutput(output, measurer));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <TOutput> TOutput getMeasurerOutput(MeasurementRunOutput runOutput,
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
