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

	public <TOutput> Iterable<TOutput> getMeasurerOutputs(
			IMeasurer<TOutput> measurer) {

		ArrayList<TOutput> result = new ArrayList<TOutput>();

		for (MeasurerOutputBase mob : getMeasurerOutputs()) {
			if (mob.isFrom(measurer))
				result.add(mob.cast(measurer));
		}

		return result;
	}

	public <TOutput> TOutput getMeasurerOutput(IMeasurer<TOutput> measurer) {
		return IterableUtils.single(getMeasurerOutputs(measurer));
	}

	public Iterable<MeasurerOutputBase> getMeasurerOutputs(MeasurerBase measurer) {

		ArrayList<MeasurerOutputBase> result = new ArrayList<MeasurerOutputBase>();

		for (MeasurerOutputBase mob : getMeasurerOutputs()) {
			if (mob.isFrom(measurer))
				result.add(mob);
		}

		return result;
	}

	public MeasurerOutputBase getMeasurerOutput(MeasurerBase measurer) {
		return IterableUtils.single(getMeasurerOutputs(measurer));
	}

	public Iterable<MeasurerSetOutput> getMeasurerSetOutputs(
			final MeasurerSet set) {
		return IterableUtils.where(getMeasurerSetOutputs(),
				new IUnaryPredicate<MeasurerSetOutput>() {

					public Boolean apply(MeasurerSetOutput arg) {
						return arg.getSetUid().equals(set.getUid());
					}
				});
	}

	public MeasurerSetOutput getMeasurerSetOutput(MeasurerSet set) {
		return IterableUtils.single(getMeasurerSetOutputs(set));
	}
}
