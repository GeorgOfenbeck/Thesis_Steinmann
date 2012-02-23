package ch.ethz.ruediste.roofline.dom;

import java.util.*;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class MeasurerSetOutput extends MeasurerSetOutputData {
	@XStreamOmitField
	private UUID setUid;

	public Iterable<MeasurerOutputBase> getMeasurerOutputs() {
		ArrayList<MeasurerOutputBase> result = new ArrayList<MeasurerOutputBase>();
		if (getMainMeasurerOutput() != null) {
			result.add(getMainMeasurerOutput());
		}
		result.addAll(getAdditionalMeasurerOutputs());
		result.addAll(getValidationMeasurerOutputs());
		return result;
	}

	/**
	 * returns the main measurer of this measurer set, in a typesafe manner. If
	 * the provided measurer was not used to generate the main measurer output
	 * of this set output, an error is thrown.
	 */
	public <T, TMeasurer extends IMeasurer<T>> T getMainMeasurerOutput(
			TMeasurer measurer) {
		if (!getMainMeasurerOutput().isFrom(measurer)) {
			throw new Error("Measurer is not main measurer of this set");
		}
		return getMainMeasurerOutput().cast(measurer);
	}

	public UUID getSetUid() {
		return setUid;
	}

	public void setSetUid(UUID measurerSetUid) {
		this.setUid = measurerSetUid;
	}
}
