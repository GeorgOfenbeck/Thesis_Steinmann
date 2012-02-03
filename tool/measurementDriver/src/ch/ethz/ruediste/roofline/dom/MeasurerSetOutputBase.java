package ch.ethz.ruediste.roofline.dom;

import java.util.ArrayList;

public class MeasurerSetOutputBase extends MeasurerSetOutputBaseData {

	public Iterable<MeasurerOutputBase> getMeasurerOutputs() {
		ArrayList<MeasurerOutputBase> result = new ArrayList<MeasurerOutputBase>();
		result.add(getMainMeasurerOutput());
		result.addAll(getAdditionalMeasurerOutputs());
		result.addAll(getValidationMeasurerOutputs());
		return result;
	}
}
