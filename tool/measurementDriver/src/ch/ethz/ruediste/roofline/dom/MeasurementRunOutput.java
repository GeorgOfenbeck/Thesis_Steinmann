package ch.ethz.ruediste.roofline.dom;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.util.*;

public class MeasurementRunOutput extends MeasurementRunOutputData {

	public Iterable<MeasurerOutputBase> getMeasurerOutputs() {
		ArrayList<MeasurerOutputBase> result = new ArrayList<MeasurerOutputBase>();
		for (MeasurerSetOutput setOutput : getMeasurerSetOutputs()) {
			result.addAll((Collection<? extends MeasurerOutputBase>) setOutput
					.getMeasurerOutputs());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <TOutput> Iterable<TOutput> getMeasurerOutputs(
			IMeasurerDescription<TOutput> measurer) {

		ArrayList<TOutput> result = new ArrayList<TOutput>();

		for (MeasurerOutputBase mob : getMeasurerOutputs()) {
			if (mob.getMeasurerId() == measurer.getId())
				result.add((TOutput) mob);
		}

		return result;
	}

	public <TOutput> TOutput getMeasurerOutput(
			IMeasurerDescription<TOutput> measurer) {
		return IterableUtils.single(getMeasurerOutputs(measurer));
	}

	public Iterable<MeasurerSetOutput> getMeasurerSetOutputs(
			final MeasurerSet set) {
		return IterableUtils.where(getMeasurerSetOutputs(),
				new IUnaryPredicate<MeasurerSetOutput>() {

					public Boolean apply(MeasurerSetOutput arg) {
						return arg.getSetId() == set.getId();
					}
				});
	}

	public MeasurerSetOutput getMeasurerSetOutput(MeasurerSet set) {
		return IterableUtils.single(getMeasurerSetOutputs(set));
	}
}
