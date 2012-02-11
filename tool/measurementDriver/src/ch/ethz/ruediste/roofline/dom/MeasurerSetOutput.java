package ch.ethz.ruediste.roofline.dom;

import java.util.*;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class MeasurerSetOutput extends MeasurerSetOutputData {
	@XStreamOmitField
	private UUID setUid;

	public Iterable<MeasurerOutputBase> getMeasurerOutputs() {
		ArrayList<MeasurerOutputBase> result = new ArrayList<MeasurerOutputBase>();
		result.add(getMainMeasurerOutput());
		result.addAll(getAdditionalMeasurerOutputs());
		result.addAll(getValidationMeasurerOutputs());
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T, TMeasurer extends IMeasurer<T>> T getMainMeasurerOutput(
			TMeasurer measurer) {
		if (getMainMeasurerOutput().getMeasurerId() != measurer.getId()) {
			throw new Error("Measurer is not main measurer of this set");
		}
		return (T) getMainMeasurerOutput();
	}

	public UUID getSetUid() {
		return setUid;
	}

	public void setSetUid(UUID measurerSetUid) {
		this.setUid = measurerSetUid;
	}
}
