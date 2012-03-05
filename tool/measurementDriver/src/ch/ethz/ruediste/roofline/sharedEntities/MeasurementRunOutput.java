package ch.ethz.ruediste.roofline.sharedEntities;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.single;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.util.*;

/**
 * the result of a measurement run
 */
public class MeasurementRunOutput extends MeasurementRunOutputData {

	/**
	 * get all measurer outputs
	 */
	public Iterable<MeasurerOutputBase> getMeasurerOutputs() {
		ArrayList<MeasurerOutputBase> result = new ArrayList<MeasurerOutputBase>();
		for (MeasurerSetOutput setOutput : getMeasurerSetOutputs()) {
			result.addAll((Collection<? extends MeasurerOutputBase>) setOutput
					.getMeasurerOutputs());
		}
		return result;
	}

	/**
	 * return the outputs of a specific measurer
	 */
	public <TOutput> Iterable<TOutput> getMeasurerOutputs(
			IMeasurer<TOutput> measurer) {

		ArrayList<TOutput> result = new ArrayList<TOutput>();

		for (MeasurerOutputBase mob : getMeasurerOutputs()) {
			if (mob.isFrom(measurer))
				result.add(mob.cast(measurer));
		}

		return result;
	}

	/**
	 * get the single output of a measurer
	 */
	public <TOutput> TOutput getMeasurerOutput(IMeasurer<TOutput> measurer) {
		return IterableUtils.single(getMeasurerOutputs(measurer));
	}

	/**
	 * get all outputs of a measurer within this run
	 */
	public Iterable<MeasurerOutputBase> getMeasurerOutputsUntyped(MeasurerBase measurer) {

		ArrayList<MeasurerOutputBase> result = new ArrayList<MeasurerOutputBase>();

		for (MeasurerOutputBase mob : getMeasurerOutputs()) {
			if (mob.isFrom(measurer))
				result.add(mob);
		}

		return result;
	}

	/**
	 * get the single output of a measurer
	 */
	public MeasurerOutputBase getMeasurerOutputUntyped(MeasurerBase measurer) {
		return single(getMeasurerOutputsUntyped(measurer));
	}

	/**
	 * get all outputs of a measurer set
	 */
	public Iterable<MeasurerSetOutput> getMeasurerSetOutputsUntyped(
			final MeasurerSet set) {
		return IterableUtils.where(getMeasurerSetOutputs(),
				new IUnaryPredicate<MeasurerSetOutput>() {

					public Boolean apply(MeasurerSetOutput arg) {
						return arg.getSetUid().equals(set.getUid());
					}
				});
	}

	/**
	 * return the single output of a measurer set. An error is thrown if there
	 * are none or multiple outputs
	 */
	public MeasurerSetOutput getMeasurerSetOutputUntyped(MeasurerSet set) {
		return IterableUtils.single(getMeasurerSetOutputsUntyped(set));
	}
}
