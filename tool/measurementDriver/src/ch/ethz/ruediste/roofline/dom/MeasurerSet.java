package ch.ethz.ruediste.roofline.dom;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.MacroKey;
import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.ParameterSpace.Coordinate;

public class MeasurerSet extends MeasurerSetData {

	public MeasurerSet() {
	}

	public MeasurerSet(MeasurerDescriptionBase measurer) {
		setMainMeasurer(measurer);
	}

	public Iterable<MeasurerDescriptionBase> getMeasurers() {
		ArrayList<MeasurerDescriptionBase> result = new ArrayList<MeasurerDescriptionBase>();
		if (getMainMeasurer() != null) {
			result.add(getMainMeasurer());
		}

		result.addAll(getAdditionalMeasurers());
		result.addAll(getValidationMeasurers());
		return result;
	}

	public Collection<? extends String> getMacroDefinitions(MacroKey key) {
		ArrayList<String> result = new ArrayList<String>();

		for (MeasurerDescriptionBase mdb : getMeasurers()) {
			if (mdb.isMacroDefined(key)) {
				result.add(mdb.getMacroDefinition(key));
			}
		}

		return result;
	}

	public void initialize(Coordinate coordinate) {
		for (MeasurerDescriptionBase mdb : getMeasurers()) {
			mdb.initialize(coordinate);
		}
	}

}
