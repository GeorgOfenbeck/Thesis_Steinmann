package ch.ethz.ruediste.roofline.dom;

import java.util.ArrayList;

public class MeasurerSetOutput extends MeasurerSetOutputData {

	public Iterable<MeasurerOutputBase> getMeasurerOutputs() {
		ArrayList<MeasurerOutputBase> result = new ArrayList<MeasurerOutputBase>();
		result.add(getMainMeasurerOutput());
		result.addAll(getAdditionalMeasurerOutputs());
		result.addAll(getValidationMeasurerOutputs());
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T, TMeasurer extends IMeasurerDescription<T>> T getMainMeasurerOutput(
			TMeasurer measurer) {
		if (getMainMeasurerOutput().getMeasurerId() != measurer.getId()) {
			throw new Error("Measurer is not main measurer of this set");
		}
		return (T) getMainMeasurerOutput();
	}
}
