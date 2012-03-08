package ch.ethz.ruediste.roofline.sharedEntities;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.ofType;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class MeasurerSet extends MeasurerSetData {

	@XStreamOmitField
	private UUID uid = UUID.randomUUID();

	public MeasurerSet() {
	}

	/**
	 * constructor setting the main measurer to the provided one
	 */
	public MeasurerSet(MeasurerBase measurer) {
		setMainMeasurer(measurer);
	}

	/**
	 * return all measurers within the set.
	 */
	public Iterable<MeasurerBase> getMeasurers() {
		LinkedHashSet<Object> set = new LinkedHashSet<Object>();
		addAll(set);
		return ofType(MeasurerBase.class, set);
	}

	/**
	 * get the macro definitions stored in all measurers
	 */
	public Collection<? extends String> getMacroDefinitions(MacroKey key) {
		ArrayList<String> result = new ArrayList<String>();

		for (MeasurerBase mdb : getMeasurers()) {
			if (mdb.isMacroDefined(key)) {
				result.add(mdb.getMacroDefinition(key));
			}
		}

		return result;
	}

	public void initialize(Coordinate coordinate) {
		for (MeasurerBase measurer : getMeasurers()) {
			measurer.initialize(coordinate);
		}
	}

	public UUID getUid() {
		return uid;
	}

	public void setUid(UUID uid) {
		this.uid = uid;
	}

}
