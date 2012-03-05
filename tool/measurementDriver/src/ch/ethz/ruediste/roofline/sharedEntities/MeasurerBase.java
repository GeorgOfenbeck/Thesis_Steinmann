package ch.ethz.ruediste.roofline.sharedEntities;

import java.util.UUID;

import ch.ethz.ruediste.roofline.measurementDriver.dom.parameterSpace.Coordinate;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class MeasurerBase extends MeasurerBaseData {

	@XStreamOmitField
	private UUID uid = UUID.randomUUID();

	public void initialize(Coordinate coordinate) {
		// TODO Auto-generated method stub

	}

	public UUID getUid() {
		return uid;
	}

	public void setUid(UUID uid) {
		this.uid = uid;
	}

}